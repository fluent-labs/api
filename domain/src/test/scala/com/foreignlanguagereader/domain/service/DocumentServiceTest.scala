package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word
import com.foreignlanguagereader.content.types.internal.word.{
  PartOfSpeech,
  Word
}
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.google.GoogleCloudClient
import com.foreignlanguagereader.domain.client.languageservice.LanguageServiceClient
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

import scala.concurrent.{ExecutionContext, Future}

class DocumentServiceTest extends AsyncFunSpec with MockitoSugar {
  val mockGoogleCloudClient: GoogleCloudClient =
    mock[GoogleCloudClient]
  val mockLanguageService: LanguageServiceClient =
    mock[LanguageServiceClient]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val documentService =
    new DocumentService(
      mockGoogleCloudClient,
      mockLanguageService,
      ec
    )

  describe("A document service") {
    it("reacts correctly to an empty document") {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerAttempt(List())))

      documentService
        .getWordsForDocument(Language.CHINESE, "some words")
        .map(result => assert(result.isEmpty))
    }

    it(
      "falls back to google cloud when the language service circuitbreaker is open"
    ) {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerNonAttempt()))
      when(
        mockGoogleCloudClient
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerAttempt(List())))

      documentService
        .getWordsForDocument(Language.CHINESE, "some words")
        .map(result => assert(result.isEmpty))
    }

    it(
      "falls back to google cloud when the language service fails"
    ) {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE_TRADITIONAL, "some words")
      ).thenReturn(
        Future.apply(
          CircuitBreakerFailedAttempt(new IllegalArgumentException("Uh oh"))
        )
      )
      when(
        mockGoogleCloudClient
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerAttempt(List())))

      documentService
        .getWordsForDocument(Language.CHINESE, "some words")
        .map(result => assert(result.isEmpty))
    }

    it("reacts correctly to the circuit breaker being closed") {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerNonAttempt()))
      when(
        mockGoogleCloudClient
          .getWordsForDocument(Language.CHINESE, "some words")
      ).thenReturn(Future.successful(CircuitBreakerNonAttempt()))

      documentService
        .getWordsForDocument(Language.CHINESE, "some words")
        .map(result => assert(result.isEmpty))
    }

    it("throws errors from when both services have failed") {
      when(
        mockLanguageService
          .getWordsForDocument(Language.CHINESE_TRADITIONAL, "some words")
      ).thenReturn(
        Future.apply(
          CircuitBreakerFailedAttempt(new IllegalArgumentException("Uh oh"))
        )
      )
      when(
        mockGoogleCloudClient
          .getWordsForDocument(Language.CHINESE_TRADITIONAL, "some words")
      ).thenReturn(
        Future.apply(
          CircuitBreakerFailedAttempt(new IllegalArgumentException("Uh oh"))
        )
      )
      documentService
        .getWordsForDocument(
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
        Future.apply(CircuitBreakerAttempt(List(testWord, phraseWord)))
      )

      documentService
        .getWordsForDocument(Language.ENGLISH, "test phrase")
        .map(result => {
          assert(result.size == 2)
          val test = result.head
          val phrase = result(1)

          assert(test == testWord)
          assert(phrase == phraseWord)
        })
    }
  }
}
