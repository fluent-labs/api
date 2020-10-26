package com.foreignlanguagereader.api.service

import cats.implicits._
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.api.client.google.GoogleCloudClient
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.word.Word
import com.foreignlanguagereader.api.service.definition.DefinitionService
import com.google.inject.Inject
import javax.inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@inject.Singleton
class DocumentService @Inject()(val googleCloudClient: GoogleCloudClient,
                                val definitionService: DefinitionService,
                                implicit val ec: ExecutionContext) {
  val logger: Logger = Logger(this.getClass)

  /*
   * Splits text into words, and then gets definitions for them.
   */
  def getWordsForDocument(wordLanguage: Language,
                          definitionLanguage: Language,
                          document: String): Future[List[Word]] =
    googleCloudClient
      .getWordsForDocument(wordLanguage, document)
      .value
      .flatMap {
        case CircuitBreakerAttempt(words) =>
          words.toList
            .traverse(
              word =>
                definitionService
                  .getDefinition(wordLanguage, definitionLanguage, word)
                  .map(d => word.copy(definitions = d))
            )
        case CircuitBreakerNonAttempt()     => Future.successful(List())
        case CircuitBreakerFailedAttempt(e) => throw e
      }
}
