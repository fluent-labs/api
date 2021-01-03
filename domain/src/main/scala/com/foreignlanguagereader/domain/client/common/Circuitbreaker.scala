package com.foreignlanguagereader.domain.client.common

import akka.actor.ActorSystem
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import cats.Functor
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus.ReadinessStatus
import play.api.Logger

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class Circuitbreaker(
    system: ActorSystem,
    implicit val ec: ExecutionContext,
    name: String,
    timeout: FiniteDuration = FiniteDuration(60, TimeUnit.SECONDS),
    resetTimeout: FiniteDuration = FiniteDuration(60, TimeUnit.SECONDS),
    maxFailures: Int = 5
) {
  val logger: Logger = Logger(this.getClass)
  logger.info(s"Initialized circuitbreaker for $name with timeout $timeout")

  val breaker: CircuitBreaker =
    new CircuitBreaker(
      system.scheduler,
      maxFailures = maxFailures,
      callTimeout = timeout,
      resetTimeout = resetTimeout
    ).onOpen(
        logger.error(s"Circuit breaker for $name opening due to failures")
      )
      .onHalfOpen(logger.info(s"Circuit breaker for $name resetting"))
      .onClose(logger.info(s"Circuit breaker for $name reset"))

  def defaultIsFailure(error: Throwable): Boolean = true
  def defaultIsSuccess[T](result: T): Boolean = true

  def onClose(body: => Unit): Unit = breaker.onClose(body)
  def isClosed: Boolean = breaker.isClosed

  def health(): ReadinessStatus =
    breaker match {
      case breaker if breaker.isClosed   => ReadinessStatus.UP
      case breaker if breaker.isHalfOpen => ReadinessStatus.DEGRADED
      case breaker if breaker.isOpen     => ReadinessStatus.DOWN
    }

  def withBreaker[T](
      logIfError: String,
      onSuccess: () => Unit,
      onError: () => Unit
  )(
      body: => Future[T]
  ): Future[CircuitBreakerResult[T]] =
    withBreaker(
      logIfError,
      defaultIsFailure,
      defaultIsSuccess[T],
      onSuccess,
      onError
    )(body)

  def withBreaker[T](
      logIfError: String,
      isFailure: Throwable => Boolean,
      isSuccess: T => Boolean,
      onSuccess: () => Unit,
      onError: () => Unit
  )(
      body: => Future[T]
  ): Future[CircuitBreakerResult[T]] =
    breaker
      .withCircuitBreaker[T](body, makeFailureFunction(isFailure, isSuccess))
      .map(s => {
        onSuccess.apply()
        CircuitBreakerAttempt(s)
      })
      .recover {
        case c: CircuitBreakerOpenException =>
          logger.warn(
            s"Failing fast because circuit breaker $name is open, ${c.remainingDuration} remaining."
          )
          onError.apply()
          CircuitBreakerNonAttempt[T]()
        case e =>
          logger.error(logIfError, e)
          onError.apply()
          CircuitBreakerFailedAttempt[T](e)
      }

  // This should return true if failure count should increase
  private[this] def makeFailureFunction[T](
      isFailure: Throwable => Boolean,
      isSuccess: T => Boolean
  ): Try[T] => Boolean = {
    case Success(result)    => !isSuccess(result)
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
