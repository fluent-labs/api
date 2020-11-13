package com.foreignlanguagereader.domain.client.common

import akka.actor.ActorSystem
import cats.implicits._
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus
import com.typesafe.config.ConfigFactory
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}

import scala.concurrent.{ExecutionContext, Future}

class BaseCircuitBreaker(
    override val system: ActorSystem,
    override val ec: ExecutionContext
) extends Circuitbreaker

class CircuitbreakerTest extends AsyncFunSpec with MockitoSugar {
  val playConfig: Configuration = Configuration(ConfigFactory.load("test.conf"))
  val application: Application = new GuiceApplicationBuilder()
    .configure(playConfig)
    .build()

  val openBreaker = new BaseCircuitBreaker(
    application.actorSystem,
    scala.concurrent.ExecutionContext.Implicits.global
  )

  def failIfNone(o: Option[String]): Boolean = o.isDefined

  def succeedForIllegalArgumentException(e: Throwable): Boolean =
    e match {
      case _: IllegalArgumentException => false
      case _                           => true
    }

  def makeBreaker(failureCount: Int): BaseCircuitBreaker = {
    val b = new BaseCircuitBreaker(
      application.actorSystem,
      scala.concurrent.ExecutionContext.Implicits.global
    )
    (1 to failureCount).foreach(_ => b.breaker.fail())
    b
  }

  describe("an open circuit breaker") {
    it("gives a correct readiness status") {
      assert(openBreaker.health() === ReadinessStatus.UP)
    }

    it("returns the results of successful calls") {
      openBreaker
        .withBreaker("Don't log")(Future.apply("testString"))
        .value
        .map {
          case CircuitBreakerAttempt("testString") => succeed
          case _                                   => fail("This is the wrong result")
        }
    }

    it("does not rethrow exceptions generated within the circuitbreaker") {
      openBreaker
        .withBreaker[String]("Uh oh")(
          Future
            .apply(throw new IllegalArgumentException("Something went wrong"))
        )
        .value
        .map {
          case CircuitBreakerFailedAttempt(e) =>
            assert(e.getMessage == "Something went wrong")
          case _ => fail("This is the wrong result")
        }
    }

    describe("with a customized failure function") {
      it(
        "does not increase the failure count for ignored exceptions"
      ) {
        val almostClosedBreaker = makeBreaker(4)

        almostClosedBreaker
          .withBreaker[String](
            "Uh oh",
            a => succeedForIllegalArgumentException(a),
            (_: String) => true
          )(
            Future
              .apply(throw new IllegalArgumentException("Something went wrong"))
          )
          .value
          .map {
            case CircuitBreakerFailedAttempt(e) =>
              assert(e.getMessage == "Something went wrong")
              assert(almostClosedBreaker.health() === ReadinessStatus.UP)
            case _ => fail("This is the wrong result")
          }
      }

      it(
        "increases the failure count for non-ignored exceptions"
      ) {
        val almostClosedBreaker = makeBreaker(4)

        almostClosedBreaker
          .withBreaker[String](
            "Uh oh",
            a => succeedForIllegalArgumentException(a),
            (_: String) => true
          )(
            Future
              .apply(throw new IllegalStateException("Something went wrong"))
          )
          .value
          .map {
            case CircuitBreakerFailedAttempt(e) =>
              assert(e.getMessage == "Something went wrong")
              assert(almostClosedBreaker.health() === ReadinessStatus.DOWN)
            case _ => fail("This is the wrong result")
          }
      }
    }

    describe("with a customized success function") {
      it(
        "increases the failure count for results that are not valid"
      ) {

        val almostClosedBreaker = makeBreaker(4)

        almostClosedBreaker
          .withBreaker[Option[String]](
            "Uh oh",
            (_: Throwable) => true,
            a => failIfNone(a)
          )(
            Future
              .apply(None)
          )
          .value
          .map {
            case CircuitBreakerAttempt(None) =>
              assert(almostClosedBreaker.health() === ReadinessStatus.DOWN)
            case _ => fail("This is the wrong result")
          }
      }

      it(
        "does not increase the failure count for results that are valid"
      ) {

        val almostClosedBreaker = makeBreaker(4)

        almostClosedBreaker
          .withBreaker[Option[String]](
            "Uh oh",
            (_: Throwable) => true,
            a => failIfNone(a)
          )(
            Future
              .apply(Some("testString"))
          )
          .value
          .map {
            case CircuitBreakerAttempt(Some("testString")) =>
              assert(almostClosedBreaker.health() === ReadinessStatus.UP)
            case _ => fail("This is the wrong result")
          }
      }
    }
  }

  describe("a closed circuit breaker") {
    val closedCircuitBreaker = makeBreaker(5)

    it("gives a correct readiness status") {
      assert(closedCircuitBreaker.health() === ReadinessStatus.DOWN)
    }

    it("closes after 5 failed attempts by default") {
      val almostClosedBreaker = makeBreaker(4)
      assert(almostClosedBreaker.health() === ReadinessStatus.UP)
      almostClosedBreaker.breaker.fail()
      assert(almostClosedBreaker.health() === ReadinessStatus.DOWN)
    }

    it("does not attempt calls") {
      closedCircuitBreaker
        .withBreaker[String]("log message")(
          Future(fail("this should not have been evaluated"))
        )
        .value
        .map {
          case CircuitBreakerNonAttempt() => succeed
          case _                          => fail("This is the wrong result")
        }
    }
  }

  describe("a circuit breaker result") {
    val success: CircuitBreakerResult[String] =
      CircuitBreakerAttempt("hello")
    val failure: CircuitBreakerResult[String] =
      CircuitBreakerFailedAttempt(new IllegalArgumentException("Uh oh"))
    val nonAttempt: CircuitBreakerResult[String] = CircuitBreakerNonAttempt()

    it("applies a function when mapping unsuccessful results") {
      success.map(_.toUpperCase()) match {
        case CircuitBreakerAttempt("HELLO") => succeed
        case _                              => fail("This is the wrong result")
      }
    }

    it("does not apply function when mapping unsuccessful results") {
      failure.map(_.toUpperCase()) match {
        case CircuitBreakerFailedAttempt(e) =>
          assert(e.getMessage == "Uh oh")
        case _ => fail("This is the wrong result")
      }
    }

    it("does not apply function when mapping a non-attempt") {
      nonAttempt.map(_.toUpperCase()) match {
        case CircuitBreakerNonAttempt() => succeed
        case _                          => fail("This is the wrong result")
      }
    }
  }
}
