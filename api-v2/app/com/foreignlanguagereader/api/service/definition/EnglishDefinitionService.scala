package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import javax.inject.Inject

import scala.concurrent.ExecutionContext

class EnglishDefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val wordLanguage: Language = Language.ENGLISH
  override val sources: Set[DefinitionSource] =
    Set(DefinitionSource.MIRRIAM_WEBSTER, DefinitionSource.WIKTIONARY)
  override val webSources: Set[DefinitionSource] =
    Set(DefinitionSource.MIRRIAM_WEBSTER, DefinitionSource.WIKTIONARY)
}
