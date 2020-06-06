package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.dto.v1.Word
import com.google.inject.Inject
import javax.inject

@inject.Singleton
class DocumentService @Inject()(
  val languageServiceClient: LanguageServiceClient
) {
  def getWordsForDocument(language: Language, document: String): List[Word] =
    languageServiceClient.getWordsForDocument(language, document)
}
