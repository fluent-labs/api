package com.foreignlanguagereader.domain.client.circuitbreaker

import akka.actor.ActorSystem
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
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
    ).onHalfOpen(logger.info(s"Circuit breaker for $name resetting"))
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
      onError: Throwable => Unit
  )(
      body: => Future[T]
  ): Future[CircuitBreakerResult[T]] =
    withBreaker(
      onError,
      defaultIsFailure,
      defaultIsSuccess[T]
    )(body)

  def withBreaker[T](
      onError: Throwable => Unit,
      isFailure: Throwable => Boolean,
      isSuccess: T => Boolean
  )(
      body: => Future[T]
  ): Future[CircuitBreakerResult[T]] =
    breaker
      .withCircuitBreaker[T](body, makeFailureFunction(isFailure, isSuccess))
      .map(s => CircuitBreakerAttempt(s))
      .recover {
        case c: CircuitBreakerOpenException =>
          logger.warn(
            s"Failing fast because circuit breaker $name is open, ${c.remainingDuration} remaining."
          )
          onError.apply(c)
          CircuitBreakerNonAttempt[T]()
        case e =>
          onError.apply(e)
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
