package com.foreignlanguagereader.api.client.common

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import cats.Functor
import cats.data.Nested
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus.ReadinessStatus
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
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

  def health(): ReadinessStatus =
    breaker match {
      case breaker if breaker.isClosed   => ReadinessStatus.UP
      case breaker if breaker.isHalfOpen => ReadinessStatus.DEGRADED
      case breaker if breaker.isOpen     => ReadinessStatus.DOWN
    }

  def withBreaker[T](
      logIfError: String,
      body: => Future[T]
  ): Nested[Future, CircuitBreakerResult, T] =
    withBreaker(logIfError, defaultIsFailure, defaultIsSuccess[T], body)

  def withBreaker[T](
      logIfError: String,
      isFailure: Throwable => Boolean,
      isSuccess: T => Boolean,
      body: => Future[T]
  ): Nested[Future, CircuitBreakerResult, T] =
    Nested[Future, CircuitBreakerResult, T](
      breaker
        .withCircuitBreaker[T](body, makeFailureFunction(isFailure, isSuccess))
        .map(s => CircuitBreakerAttempt(s))
        .recover {
          case c: CircuitBreakerOpenException =>
            logger.warn(
              s"Failing fast because circuit breaker is open, ${c.remainingDuration} remaining."
            )
            CircuitBreakerNonAttempt()
          case e =>
            logger.error(logIfError)
            CircuitBreakerFailedAttempt(e)
        }
    )

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
case class CircuitBreakerFailedAttempt[T](e: Throwable)
    extends CircuitBreakerResult[T]
case class CircuitBreakerNonAttempt[T]() extends CircuitBreakerResult[T]

object CircuitBreakerResult {
  implicit def functor: Functor[CircuitBreakerResult] =
    new Functor[CircuitBreakerResult] {
      override def map[A, B](
          fa: CircuitBreakerResult[A]
      )(f: A => B): CircuitBreakerResult[B] =
        fa match {
          case CircuitBreakerNonAttempt()     => CircuitBreakerNonAttempt()
          case CircuitBreakerFailedAttempt(e) => CircuitBreakerFailedAttempt(e)
          case CircuitBreakerAttempt(result)  => CircuitBreakerAttempt(f(result))
        }
    }
}
