package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  DefinitionSource,
  EnglishDefinition
}
import com.foreignlanguagereader.content.types.internal.word
import com.foreignlanguagereader.content.types.internal.word.{
  PartOfSpeech,
  Word
}
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.google.GoogleCloudClient
import com.foreignlanguagereader.domain.client.languageservice.LanguageServiceClient
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

import scala.concurrent.{ExecutionContext, Future}

class DocumentServiceTest extends AsyncFunSpec with MockitoSugar {
  val mockGoogleCloudClient: GoogleCloudClient =
    mock[GoogleCloudClient]
  val mockLanguageService: LanguageServiceClient =
    mock[LanguageServiceClient]
  val mockDefinitionService: DefinitionService =
    mock[DefinitionService]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val documentService =
    new DocumentService(
      mockGoogleCloudClient,
      mockLanguageService,
      mockDefinitionService,
      ec
    )

  describe("A document service") {
    it("reacts correctly to an empty document") {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerAttempt(Set())))

      documentService
        .getWordsForDocument(Language.CHINESE, Language.CHINESE, "some words")
        .map(result => assert(result.isEmpty))
    }

    it("reacts correctly to the circuit breaker being closed") {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerNonAttempt()))

      documentService
        .getWordsForDocument(Language.CHINESE, Language.CHINESE, "some words")
        .map(result => assert(result.isEmpty))
    }

    it("throws errors from language service") {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE_TRADITIONAL, "some words")
      ).thenReturn(
        Future.apply(
          CircuitBreakerFailedAttempt(new IllegalArgumentException("Uh oh"))
        )
      )
      documentService
        .getWordsForDocument(
          Language.CHINESE_TRADITIONAL,
          Language.CHINESE_TRADITIONAL,
          "some words"
        )
        .map(_ => fail("This should have failed"))
        .recover {
          case err: IllegalArgumentException =>
            assert(err.getMessage == "Uh oh")
          case _ => fail("This is the wrong exception")
        }
    }

    it("can get words for a document") {
      val testWord = word.Word(
        language = Language.ENGLISH,
        token = "test",
        tag = PartOfSpeech.VERB,
        lemma = "test",
        definitions = List(),
        gender = None,
        number = None,
        proper = None,
        tense = None,
        processedToken = "test"
      )
      val phraseWord = Word(
        language = Language.ENGLISH,
        token = "phrase",
        tag = PartOfSpeech.NOUN,
        lemma = "phrase",
        definitions = List(),
        gender = None,
        number = None,
        proper = None,
        tense = None,
        processedToken = "phrase"
      )
      when(
        mockLanguageService
          .getWordsForDocument(Language.ENGLISH, "test phrase")
      ).thenReturn(
        Future.apply(CircuitBreakerAttempt(Set(testWord, phraseWord)))
      )

      val testDefinition = EnglishDefinition(
        subdefinitions = List("test"),
        ipa = "",
        tag = PartOfSpeech.NOUN,
        examples = None,
        wordLanguage = Language.ENGLISH,
        definitionLanguage = Language.SPANISH,
        source = DefinitionSource.MULTIPLE,
        token = "test"
      )
      when(
        mockDefinitionService
          .getDefinition(Language.ENGLISH, Language.SPANISH, testWord)
      ).thenReturn(Future.successful(List(testDefinition)))

      val phraseDefinition = EnglishDefinition(
        subdefinitions = List("phrase"),
        ipa = "",
        tag = PartOfSpeech.VERB,
        examples = None,
        wordLanguage = Language.ENGLISH,
        definitionLanguage = Language.SPANISH,
        source = DefinitionSource.MULTIPLE,
        token = "phrase"
      )
      when(
        mockDefinitionService
          .getDefinition(Language.ENGLISH, Language.SPANISH, phraseWord)
      ).thenReturn(Future.successful(List(phraseDefinition)))

      documentService
        .getWordsForDocument(Language.ENGLISH, Language.SPANISH, "test phrase")
        .map(result => {
          assert(result.size == 2)
          val test = result.head
          val phrase = result(1)

          assert(test == testWord.copy(definitions = List(testDefinition)))
          assert(
            phrase == phraseWord
              .copy(definitions = List(phraseDefinition))
          )
        })
    }
  }
}
