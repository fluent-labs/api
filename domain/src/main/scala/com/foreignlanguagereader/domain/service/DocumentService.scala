package com.foreignlanguagereader.domain.service

import cats.implicits._
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.google.GoogleCloudClient
import com.foreignlanguagereader.domain.client.languageservice.LanguageServiceClient
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import com.google.inject.Inject

import javax.inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@inject.Singleton
class DocumentService @Inject() (
    val googleCloudClient: GoogleCloudClient,
    languageServiceClient: LanguageServiceClient,
    val definitionService: DefinitionService,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  /*
   * Splits text into words, and then gets definitions for them.
   */
  def getWordsForDocument(
      wordLanguage: Language,
      definitionLanguage: Language,
      document: String
  ): Future[List[Word]] =
    getWordsFromLanguageService(wordLanguage, document)

  def getWordsFromGoogleCloud(
      language: Language,
      document: String
  ): Future[Set[Word]] = {
    googleCloudClient
      .getWordsForDocument(language, document)
      .map {
        case CircuitBreakerAttempt(result)  => result
        case CircuitBreakerNonAttempt()     => Set[Word]()
        case CircuitBreakerFailedAttempt(e) => throw e
      }
  }

  def getWordsFromLanguageService(
      language: Language,
      document: String
  ): Future[List[Word]] = {
    languageServiceClient
      .getWordsForDocument(language, document)
      .map {
        case CircuitBreakerAttempt(result)  => result
        case CircuitBreakerNonAttempt()     => List[Word]()
        case CircuitBreakerFailedAttempt(e) => throw e
      }
  }
}
