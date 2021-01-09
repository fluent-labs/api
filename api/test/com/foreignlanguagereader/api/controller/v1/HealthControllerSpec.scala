package com.foreignlanguagereader.api.controller.v1

import com.foreignlanguagereader.domain.metrics.MetricsReporter
import org.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._

class HealthControllerSpec extends PlaySpec with MockitoSugar {

  val jsonContentType = "application/json"
  val textContentType = "text/plain"

  val app: Application = new GuiceApplicationBuilder()
    .bindings(bind[MetricsReporter].toInstance(mock[MetricsReporter]))
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
}
