package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.client.GoogleCloudClient
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.dto.v1.document.WordDTO
import com.foreignlanguagereader.api.service.definition.DefinitionService
import com.google.inject.Inject
import javax.inject

import scala.concurrent.{ExecutionContext, Future}

@inject.Singleton
class DocumentService @Inject()(val googleCloudClient: GoogleCloudClient,
                                val definitionService: DefinitionService,
                                implicit val ec: ExecutionContext) {
  def getWordsForDocument(wordLanguage: Language,
                          definitionLanguage: Language,
                          document: String): Future[Option[Seq[WordDTO]]] =
    (googleCloudClient.getWordsForDocument(wordLanguage, document) map {
      case Some(words) =>
        Future
          .sequence(
            words.toList
              .map(
                word =>
                  definitionService
                    .getDefinition(wordLanguage, definitionLanguage, word.token)
                    .map(d => {
                      word.copy(definitions = d)
                    })
              )
          )
          .map(words => Some(words.map(_.toDTO)))
      case None => Future.successful(None)
    }).flatten
}
