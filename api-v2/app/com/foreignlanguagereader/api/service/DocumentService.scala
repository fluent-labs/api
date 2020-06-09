package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.dto.v1.Word
import com.google.inject.Inject
import javax.inject

import scala.concurrent.Future

@inject.Singleton
class DocumentService @Inject()(
  val languageServiceClient: LanguageServiceClient
) {
  def getWordsForDocument(language: Language,
                          document: String): Future[Seq[Word]] =
    languageServiceClient.getWordsForDocument(language, document)
}
