package com.foreignlanguagereader.domain.service

import cats.implicits._
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.google.GoogleCloudClient
import com.foreignlanguagereader.content.types.Language.{
  CHINESE,
  CHINESE_TRADITIONAL,
  Language
}
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.spark.SparkNLPClient
import com.google.inject.Inject
import javax.inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@inject.Singleton
class DocumentService @Inject() (
    val googleCloudClient: GoogleCloudClient,
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
    tokenizeDocument(wordLanguage, document).flatMap(words => {
      words.toList
        .traverse(word =>
          definitionService
            .getDefinition(wordLanguage, definitionLanguage, word)
            .map(d => word.copy(definitions = d))
        )
    })

  def tokenizeDocument(
      language: Language,
      document: String
  ): Future[Set[Word]] =
    language match {
      // Spark NLP currently doesn't have a good way to tokenize Chinese
      // So we fall back to Google cloud
      case CHINESE             => getWordsFromGoogleCloud(language, document)
      case CHINESE_TRADITIONAL => getWordsFromGoogleCloud(language, document)
      case _                   => Future.apply(SparkNLPClient.lemmatize(language, document))
    }

  def getWordsFromGoogleCloud(
      language: Language,
      document: String
  ): Future[Set[Word]] = {
    googleCloudClient
      .getWordsForDocument(language, document)
      .value
      .map {
        case CircuitBreakerAttempt(result)  => result
        case CircuitBreakerNonAttempt()     => Set[Word]()
        case CircuitBreakerFailedAttempt(e) => throw e
      }
  }
}
