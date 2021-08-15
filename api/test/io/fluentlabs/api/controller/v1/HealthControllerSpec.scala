package io.fluentlabs.api.controller.v1

import io.fluentlabs.domain.metrics.MetricsReporter
import io.fluentlabs.domain.service.AuthenticationService
import org.mockito.MockitoSugar
import pdi.jwt.JwtClaim
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._

import scala.util.{Failure, Success}

class HealthControllerSpec extends PlaySpec with MockitoSugar {

  val jsonContentType = "application/json"
  val textContentType = "text/plain"

  val mockAuthenticationService: AuthenticationService =
    mock[AuthenticationService]

  val app: Application = new GuiceApplicationBuilder()
    .bindings(bind[MetricsReporter].toInstance(mock[MetricsReporter]))
    .bindings(bind[AuthenticationService].toInstance(mockAuthenticationService))
    .build()

  "Health Check" should {
    val healthRoute = "/health"
    val upStatus = "{\"status\":\"up\"}"

    "render the health check page from the router" in {
      val request = FakeRequest(GET, healthRoute)
      val health = route(app, request).get

      status(health) mustBe OK
      contentType(health) mustBe Some(jsonContentType)
      contentAsString(health) must include(upStatus)
    }
  }

  "Readiness Check" should {
    val readinessRoute = "/readiness"
    val upStatus = "{\"database\":\"UP\",\"content\":\"UP\",\"webster\":\"UP\"}"

    "render the readiness page from the router" in {
      val request = FakeRequest(GET, readinessRoute)
      val readiness = route(app, request).get

      status(readiness) mustBe OK
      contentType(readiness) mustBe Some(jsonContentType)
      contentAsString(readiness) must include(upStatus)
    }
  }

  "Metrics endpoint" should {
    val metricsRoute = "/metrics"

    "render the metrics page from the router" in {
      val request = FakeRequest(GET, metricsRoute)
      val metrics = route(app, request).get

      status(metrics) mustBe OK
      contentType(metrics) mustBe Some(textContentType)
    }
  }

  "Auth endpoint" should {
    val authRoute = "/auth"

    "give an error if no auth was passed" in {
      val request = FakeRequest(GET, authRoute)
      val auth = route(app, request).get

      status(auth) mustBe UNAUTHORIZED
      contentType(auth) mustBe Some(textContentType)
    }

    "give an error if auth is invalid" in {
      when(mockAuthenticationService.validateJwt("part.part.part"))
        .thenReturn(Failure(new IllegalArgumentException("Stop")))

      val request =
        FakeRequest(GET, authRoute).withHeaders(
          ("Authorization", "Bearer part.part.part")
        )
      val auth = route(app, request).get

      status(auth) mustBe UNAUTHORIZED
      contentType(auth) mustBe Some(textContentType)
    }

    "returns a response if auth is successful" in {
      when(mockAuthenticationService.validateJwt("part.part.part"))
        .thenReturn(
          Success(
            new JwtClaim(
              "content",
              issuer = None,
              subject = None,
              audience = None,
              expiration = None,
              notBefore = None,
              issuedAt = None,
              jwtId = None
            )
          )
        )

      val request =
        FakeRequest(GET, authRoute).withHeaders(
          ("Authorization", "Bearer part.part.part")
        )
      val auth = route(app, request).get

      status(auth) mustBe OK
      contentType(auth) mustBe Some(jsonContentType)
    }
  }
}
