package com.foreignlanguagereader.api.controller.v1

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.OptionValues
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test._

// Shim because scalatestplus is behind scalatest, so dependencies collide
// More: https://github.com/scalatest/scalatest/issues/1734
abstract class PlaySpec
    extends AnyWordSpec
    with Matchers
    with OptionValues
    with WsScalaTestClient

class HealthControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  val responseContentType = "application/json"

  "Health Check" should {
    val healthRoute = "/health"
    val upStatus = "{\"status\":\"up\"}"

    "render the index page from the application" in {
      val controller = inject[HealthController]
      val home = controller.health().apply(FakeRequest(GET, healthRoute))

      status(home) mustBe OK
      contentType(home) mustBe Some(responseContentType)
      contentAsString(home) must include(upStatus)
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, healthRoute)
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some(responseContentType)
      contentAsString(home) must include(upStatus)
    }
  }
}
