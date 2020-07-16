package com.foreignlanguagereader.api.client.common

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
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

  def health(): ReadinessStatus = breaker match {
    case breaker if breaker.isClosed   => ReadinessStatus.UP
    case breaker if breaker.isHalfOpen => ReadinessStatus.DEGRADED
    case breaker if breaker.isOpen     => ReadinessStatus.DOWN
  }

  def withBreaker[T](
    body: => Future[T],
    isFailure: Throwable => Boolean = _ => true
  ): Future[Try[CircuitBreakerResult[T]]] =
    breaker
      .withCircuitBreaker[T](body, makeFailureFunction(isFailure))
      .transform(wrapWithAttemptInformation)
      .map(s => Success(s))
      .recover(e => Failure(e))

  private[this] def wrapWithAttemptInformation[T](
    t: Try[T]
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
    isFailure: Throwable => Boolean
  ): Try[T] => Boolean = {
    case Success(_)         => false
    case Failure(exception) => isFailure(exception)
  }
}

sealed abstract class CircuitBreakerResult[T] {
  def transform[U](transformer: T => U): CircuitBreakerResult[U]
}
case class CircuitBreakerAttempt[T](result: T) extends CircuitBreakerResult[T] {
  def transform[U](transformer: T => U): CircuitBreakerAttempt[U] =
    CircuitBreakerAttempt(transformer(result))
}
case class CircuitBreakerNonAttempt[T]() extends CircuitBreakerResult[T] {
  def transform[U](transformer: T => U): CircuitBreakerNonAttempt[U] =
    CircuitBreakerNonAttempt()
}
