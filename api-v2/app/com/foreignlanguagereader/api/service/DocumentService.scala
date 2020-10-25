package com.foreignlanguagereader.api.service

import cats.implicits._
import com.foreignlanguagereader.api.client.common.CircuitBreakerAttempt
import com.foreignlanguagereader.api.client.google.GoogleCloudClient
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.word.Word
import com.foreignlanguagereader.api.service.definition.DefinitionService
import com.google.inject.Inject
import javax.inject

import scala.concurrent.{ExecutionContext, Future}

@inject.Singleton
class DocumentService @Inject()(val googleCloudClient: GoogleCloudClient,
                                val definitionService: DefinitionService,
                                implicit val ec: ExecutionContext) {
  /*
   * Splits text into words, and then gets definitions for them.
   */
  def getWordsForDocument(wordLanguage: Language,
                          definitionLanguage: Language,
                          document: String): Future[List[Word]] =
    googleCloudClient.getWordsForDocument(wordLanguage, document) flatMap {
      case CircuitBreakerAttempt(words) =>
        // Requests go out at the same time, and then we block until they are all done.
        words.toList
          .traverse(
            word =>
              definitionService
                .getDefinition(wordLanguage, definitionLanguage, word)
                .map(d => word.copy(definitions = d))
          )
      case _ => Future.successful(List())
    }
}
