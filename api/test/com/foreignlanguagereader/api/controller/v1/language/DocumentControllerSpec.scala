package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.controller.v1.PlaySpec
import com.foreignlanguagereader.api.error.ServiceException
import io.fluentlabs.content.types.Language
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.RequestPath
import com.foreignlanguagereader.domain.service.DocumentService
import io.fluentlabs.dto.v1.document.DocumentRequest
import io.prometheus.client.Histogram
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.Outcome
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import play.libs.{Json => JavaJson}

import scala.concurrent.Future

class DocumentControllerSpec extends PlaySpec with MockitoSugar {

  val mockMetricsReporter: MetricsReporter = mock[MetricsReporter]
  val mockDocumentService: DocumentService = mock[DocumentService]

  val app: Application = new GuiceApplicationBuilder()
    .bindings(bind[MetricsReporter].toInstance(mockMetricsReporter))
    .bindings(bind[DocumentService].toInstance(mockDocumentService))
    .build()

  override def withFixture(test: NoArgTest): Outcome = {
    Mockito.reset(mockMetricsReporter)
    super.withFixture(test)
  }

  "Definitions endpoints" should {
    val goodRequest = "/v1/language/document/ENGLISH/"

    "get good requests from the router" in {
      val document = "This is a test sentence."

      when(
        mockDocumentService
          .getWordsForDocument(
            Language.ENGLISH,
            document
          )
      ).thenReturn(Future.successful(List()))

      val mockTimer = Some(mock[Histogram.Timer])
      when(
        mockMetricsReporter.reportRequestStarted("POST", RequestPath.DOCUMENT)
      ).thenReturn(mockTimer)

      val documentRequest =
        JavaJson.stringify(JavaJson.toJson(new DocumentRequest(document)))
      val request =
        FakeRequest(POST, goodRequest).withJsonBody(Json.parse(documentRequest))
      val goodResponse = route(app, request).get

      status(goodResponse) mustBe OK
      contentAsString(goodResponse) must include("[]")
    }

    "appropriately handle bad requests from the router" in {

      val mockTimer = Some(mock[Histogram.Timer])
      when(
        mockMetricsReporter.reportRequestStarted("POST", RequestPath.DOCUMENT)
      ).thenReturn(mockTimer)

      val badRequest =
        JavaJson.stringify(JavaJson.toJson(new ServiceException("Uh oh")))
      val request =
        FakeRequest(POST, goodRequest).withJsonBody(Json.parse(badRequest))
      val badResponse = route(app, request).get

      status(badResponse) mustBe 400
      contentAsString(badResponse) must include(
        "{\"message\":\"Invalid request body, please try again\"}"
      )
    }
  }
}
