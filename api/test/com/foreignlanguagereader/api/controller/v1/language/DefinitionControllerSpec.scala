package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.controller.v1.PlaySpec
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import io.prometheus.client.Histogram
import org.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class DefinitionControllerSpec extends PlaySpec with MockitoSugar {

  val mockMetricsReporter: MetricsReporter = mock[MetricsReporter]
  val mockDefinitionService: DefinitionService = mock[DefinitionService]

  val app: Application = new GuiceApplicationBuilder()
    .bindings(bind[MetricsReporter].toInstance(mockMetricsReporter))
    .bindings(bind[DefinitionService].toInstance(mockDefinitionService))
    .build()

  "Definitions endpoints" should {
    val goodRequest = "/v1/language/definition/SPANISH/test/"
    val badLanguageRequest = "/v1/language/definition/ELEPHANT/test/"

    "get good requests from the router" in {
      when(
        mockDefinitionService
          .getDefinition(
            Language.SPANISH,
            Language.ENGLISH,
            Word.fromToken("test", Language.SPANISH)
          )
      ).thenReturn(Future.successful(List()))

      val mockTimer = mock[Histogram.Timer]
      when(mockMetricsReporter.startTimer("GET", "definition"))
        .thenReturn(Some(mockTimer))

      val request = FakeRequest(GET, goodRequest)
      val goodResponse = route(app, request).get

      status(goodResponse) mustBe OK
      contentAsString(goodResponse) must include("[]")
    }
  }
}
