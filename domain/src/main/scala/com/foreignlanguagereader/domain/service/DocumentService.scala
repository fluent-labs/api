package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.google.GoogleCloudClient
import com.foreignlanguagereader.domain.client.languageservice.LanguageServiceClient
import com.google.inject.Inject

import javax.inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@inject.Singleton
class DocumentService @Inject() (
    val googleCloudClient: GoogleCloudClient,
    languageServiceClient: LanguageServiceClient,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  /*
   * Splits text into words, and then gets definitions for them.
   */
  def getWordsForDocument(
      wordLanguage: Language,
      document: String
  ): Future[List[Word]] =
    languageServiceClient
      .getWordsForDocument(wordLanguage, document)
      .map {
        case CircuitBreakerAttempt(result) =>
          Future.successful(CircuitBreakerAttempt(result))
        case _ => googleCloudClient.getWordsForDocument(wordLanguage, document)
      }
      .flatten
      .map {
        case CircuitBreakerAttempt(result)  => result
        case CircuitBreakerNonAttempt()     => List[Word]()
        case CircuitBreakerFailedAttempt(e) => throw e
      }
}
