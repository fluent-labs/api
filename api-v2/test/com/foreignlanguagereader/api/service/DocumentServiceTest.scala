package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.client.google.GoogleCloudClient
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.word.Word
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

    it("can get words for a document") {
      val testWord = Word(
        language = Language.ENGLISH,
        token = "test",
        tag = None,
        lemma = "test",
        definitions = None,
        gender = None,
        number = None,
        proper = None,
        tense = None,
        processedToken = "test"
      )
      val phraseWord = Word(
        language = Language.ENGLISH,
        token = "phrase",
        tag = None,
        lemma = "phrase",
        definitions = None,
        gender = None,
        number = None,
        proper = None,
        tense = None,
        processedToken = "phrase"
      )
      when(
        mockGoogleCloudClient
          .getWordsForDocument(Language.ENGLISH, "test phrase")
      ).thenReturn(Future.successful(Some(Set(testWord, phraseWord))))

      val testDefinition = Definition(
        subdefinitions = List("test"),
        ipa = "",
        tag = None,
        examples = None,
        wordLanguage = Language.ENGLISH,
        definitionLanguage = Language.SPANISH,
        source = DefinitionSource.MULTIPLE,
        token = "test"
      )
      when(
        mockDefinitionService
          .getDefinition(Language.ENGLISH, Language.SPANISH, testWord)
      ).thenReturn(Future.successful(Some(List(testDefinition))))

      val phraseDefinition = Definition(
        subdefinitions = List("phrase"),
        ipa = "",
        tag = None,
        examples = None,
        wordLanguage = Language.ENGLISH,
        definitionLanguage = Language.SPANISH,
        source = DefinitionSource.MULTIPLE,
        token = "phrase"
      )
      when(
        mockDefinitionService
          .getDefinition(Language.ENGLISH, Language.SPANISH, phraseWord)
      ).thenReturn(Future.successful(Some(List(phraseDefinition))))

      documentService
        .getWordsForDocument(Language.ENGLISH, Language.SPANISH, "test phrase")
        .map(result => {
          assert(result.isDefined)
          val test = result.get(0)
          val phrase = result.get(1)

          assert(
            test == testWord.copy(definitions = Some(List(testDefinition)))
          )
          assert(
            phrase == phraseWord
              .copy(definitions = Some(List(phraseDefinition)))
          )
        })
    }
  }
}
