package com.foreignlanguagereader.api.v1

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.controller.v1.HealthController
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test._

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
