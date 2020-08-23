package com.foreignlanguagereader.api.client.common

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import cats.{Applicative, Functor}
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus.ReadinessStatus
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

trait Circuitbreaker {
  val defaultTimeout = 60
  val defaultResetTimeout = 60

  val system: ActorSystem
  implicit val ec: ExecutionContext
  val maxFailures = 5
  val timeout: FiniteDuration = FiniteDuration(defaultTimeout, TimeUnit.SECONDS)
  val resetTimeout: FiniteDuration =
    FiniteDuration(defaultResetTimeout, TimeUnit.SECONDS)

  val logger: Logger = Logger(this.getClass)

  val breaker: CircuitBreaker =
    new CircuitBreaker(
      system.scheduler,
      maxFailures = maxFailures,
      callTimeout = timeout,
      resetTimeout = resetTimeout
    ).onOpen(logger.error("Circuit breaker opening due to failures"))
      .onHalfOpen(logger.info("Circuit breaker resetting"))
      .onClose(logger.info("Circuit breaker reset"))

  def defaultIsFailure(error: Throwable): Boolean = true
  def defaultIsSuccess[T](result: T): Boolean = true

  def health(): ReadinessStatus = breaker match {
    case breaker if breaker.isClosed   => ReadinessStatus.UP
    case breaker if breaker.isHalfOpen => ReadinessStatus.DEGRADED
    case breaker if breaker.isOpen     => ReadinessStatus.DOWN
  }

  def withBreaker[T](body: => Future[T]): Future[Try[CircuitBreakerResult[T]]] =
    withBreaker(defaultIsFailure, defaultIsSuccess[T], body)

  def withBreaker[T](isFailure: Throwable => Boolean,
                     isSuccess: T => Boolean,
                     body: => Future[T]): Future[Try[CircuitBreakerResult[T]]] =
    breaker
      .withCircuitBreaker[T](body, makeFailureFunction(isFailure, isSuccess))
      .transform(wrapWithAttemptInformation)
      .map(s => Success(s))
      .recover(e => Failure(e))

  private[this] def wrapWithAttemptInformation[T](
    t: Try[T],
  ): Try[CircuitBreakerResult[T]] = t match {
    case Success(s) => Success(CircuitBreakerAttempt[T](s))
    case Failure(e) =>
      e match {
        case _: CircuitBreakerOpenException =>
          logger.warn("Failing fast because circuit breaker is open.")
          Success(CircuitBreakerNonAttempt[T]())
        case attempt: Exception => Failure(attempt)
      }
  }

  private[this] def makeFailureFunction[T](
    isFailure: Throwable => Boolean,
    isSuccess: T => Boolean
  ): Try[T] => Boolean = {
    case Success(result)    => isSuccess(result)
    case Failure(exception) => isFailure(exception)
  }
}

sealed trait CircuitBreakerResult[T]
case class CircuitBreakerAttempt[T](result: T) extends CircuitBreakerResult[T]
case class CircuitBreakerNonAttempt[T]() extends CircuitBreakerResult[T]

object CircuitBreakerResult {
  implicit def functor: Functor[CircuitBreakerResult] =
    new Functor[CircuitBreakerResult] {
      override def map[A, B](
        fa: CircuitBreakerResult[A]
      )(f: A => B): CircuitBreakerResult[B] = fa match {
        case CircuitBreakerNonAttempt()    => CircuitBreakerNonAttempt()
        case CircuitBreakerAttempt(result) => CircuitBreakerAttempt(f(result))
      }
    }

  implicit def applicative: Applicative[CircuitBreakerResult] =
    new Applicative[CircuitBreakerResult] {
      override def pure[A](x: A): CircuitBreakerResult[A] =
        CircuitBreakerAttempt(x)

      override def ap[A, B](
        ff: CircuitBreakerResult[A => B]
      )(fa: CircuitBreakerResult[A]): CircuitBreakerResult[B] = fa match {
        case CircuitBreakerNonAttempt() => CircuitBreakerNonAttempt()
        case CircuitBreakerAttempt(result) =>
          ff match {
            case CircuitBreakerAttempt(fn)  => CircuitBreakerAttempt(fn(result))
            case CircuitBreakerNonAttempt() => CircuitBreakerNonAttempt()
          }
      }
    }
}
