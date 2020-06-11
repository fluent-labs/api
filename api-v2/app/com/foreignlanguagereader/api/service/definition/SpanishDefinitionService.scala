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

class SpanishDefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val wordLanguage: Language = Language.SPANISH
  override val sources: Set[DefinitionSource] =
    Set(DefinitionSource.WIKTIONARY)
  override val webSources: Set[DefinitionSource] = Set(
    DefinitionSource.WIKTIONARY
  )
}
