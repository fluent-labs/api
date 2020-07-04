package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.client.google.GoogleCloudClient
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.service.definition.DefinitionService
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class DocumentServiceTest extends AsyncFunSpec with MockitoSugar {
  val mockGoogleCloudClient: GoogleCloudClient =
    mock[GoogleCloudClient]
  val mockDefinitionService: DefinitionService =
    mock[DefinitionService]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val documentService =
    new DocumentService(mockGoogleCloudClient, mockDefinitionService, ec)

  describe("A document service") {
    it("reacts correctly to an empty document") {
      when(
        mockGoogleCloudClient
          .getWordsForDocument(Language.ENGLISH, "some words")
      ).thenReturn(Future.successful(None))

      documentService
        .getWordsForDocument(Language.ENGLISH, Language.ENGLISH, "some words")
        .map(result => assert(result.isEmpty))
    }

//    it("can get words for a document") {}
  }
}
