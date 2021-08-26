package io.fluentlabs.domain.client.common

import cats.syntax.all._
import io.fluentlabs.dto.v1.health.ReadinessStatus
import com.typesafe.config.ConfigFactory
import io.fluentlabs.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult,
  Circuitbreaker
}
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}

import scala.concurrent.Future

class CircuitbreakerTest extends AsyncFunSpec with MockitoSugar {
  val playConfig: Configuration = Configuration(ConfigFactory.load("test.conf"))
  val application: Application = new GuiceApplicationBuilder()
    .configure(playConfig)
    .build()

  val closedBreaker = new Circuitbreaker(
    application.actorSystem,
    scala.concurrent.ExecutionContext.Implicits.global,
    "ClosedBreaker"
  )

  def failIfNone(o: Option[String]): Boolean = o.isDefined

  def succeedForIllegalArgumentException(e: Throwable): Boolean =
    e match {
      case _: IllegalArgumentException => false
      case _                           => true
    }

  def makeBreaker(failureCount: Int, name: String): Circuitbreaker = {
    val b = new Circuitbreaker(
      application.actorSystem,
      scala.concurrent.ExecutionContext.Implicits.global,
      name
    )
    (1 to failureCount).foreach(_ => b.breaker.fail())
    b
  }

  val noOp: Throwable => Unit = _ => ()

  describe("a closed circuit breaker") {
    it("gives a correct readiness status") {
      assert(closedBreaker.health() === ReadinessStatus.UP)
    }

    it("returns the results of successful calls") {
      closedBreaker
        .withBreaker(noOp)(
          Future.apply("testString")
        )
        .map {
          case CircuitBreakerAttempt("testString") => succeed
          case _ => fail("This is the wrong result")
        }
    }

    it("does not rethrow exceptions generated within the circuitbreaker") {
      closedBreaker
        .withBreaker[String](noOp)(
          Future
            .apply(throw new IllegalArgumentException("Something went wrong"))
        )
        .map {
          case CircuitBreakerFailedAttempt(e) =>
            assert(e.getMessage == "Something went wrong")
            succeed
          case _ => fail("This is the wrong result")
        }
    }

    it("calls error callback on failure") {
      var called: Option[Throwable] = None
      val correctMethod = (e: Throwable) => { called = Some(e) }

      closedBreaker
        .withBreaker[String](correctMethod)(
          Future
            .apply(throw new IllegalArgumentException("Something went wrong"))
        )
        .map {
          case CircuitBreakerFailedAttempt(e) => assert(called.contains(e))
          case _ => fail("This is the wrong result")
        }
    }

    describe("with a customized failure function") {
      it(
        "does not increase the failure count for ignored exceptions"
      ) {
        val almostOpenBreaker = makeBreaker(4, "AlmostOpen")

        almostOpenBreaker
          .withBreaker[String](
            noOp,
            a => succeedForIllegalArgumentException(a),
            (_: String) => true
          )(
            Future
              .apply(throw new IllegalArgumentException("Something went wrong"))
          )
          .map {
            case CircuitBreakerFailedAttempt(e) =>
              assert(e.getMessage == "Something went wrong")
              assert(almostOpenBreaker.health() === ReadinessStatus.UP)
            case _ => fail("This is the wrong result")
          }
      }

      it(
        "increases the failure count for non-ignored exceptions"
      ) {
        val almostOpenBreaker = makeBreaker(4, "AlmostOpen")

        almostOpenBreaker
          .withBreaker[String](
            noOp,
            a => succeedForIllegalArgumentException(a),
            (_: String) => true
          )(
            Future
              .apply(throw new IllegalStateException("Something went wrong"))
          )
          .map {
            case CircuitBreakerFailedAttempt(e) =>
              assert(e.getMessage == "Something went wrong")
              assert(almostOpenBreaker.health() === ReadinessStatus.DOWN)
            case _ => fail("This is the wrong result")
          }
      }
    }

    describe("with a customized success function") {
      it(
        "increases the failure count for results that are not valid"
      ) {

        val almostOpenBreaker = makeBreaker(4, "AlmostOpen")

        almostOpenBreaker
          .withBreaker[Option[String]](
            noOp,
            (_: Throwable) => true,
            a => failIfNone(a)
          )(
            Future
              .apply(None)
          )
          .map {
            case CircuitBreakerAttempt(None) =>
              assert(almostOpenBreaker.health() === ReadinessStatus.DOWN)
            case _ => fail("This is the wrong result")
          }
      }

      it(
        "does not increase the failure count for results that are valid"
      ) {

        val almostOpenBreaker = makeBreaker(4, "AlmostOpen")

        almostOpenBreaker
          .withBreaker[Option[String]](
            noOp,
            (_: Throwable) => true,
            a => failIfNone(a)
          )(
            Future
              .apply(Some("testString"))
          )
          .map {
            case CircuitBreakerAttempt(Some("testString")) =>
              assert(almostOpenBreaker.health() === ReadinessStatus.UP)
            case _ => fail("This is the wrong result")
          }
      }
    }
  }

  describe("an open circuit breaker") {
    val openCircuitBreaker = makeBreaker(5, "Open")

    it("gives a correct readiness status") {
      assert(openCircuitBreaker.health() === ReadinessStatus.DOWN)
    }

    it("opens after 5 failed attempts by default") {
      val almostOpenBreaker = makeBreaker(4, "AlmostOpen")
      assert(almostOpenBreaker.health() === ReadinessStatus.UP)
      almostOpenBreaker.breaker.fail()
      assert(almostOpenBreaker.health() === ReadinessStatus.DOWN)
    }

    it("does not attempt calls") {
      openCircuitBreaker
        .withBreaker[String](noOp)(
          Future(fail("this should not have been evaluated"))
        )
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
