package com.foreignlanguagereader.domain.service.definition

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.definition.{
  DefinitionSource,
  EnglishDefinition
}
import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchCacheClient
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.fetcher.english.{
  WebsterEnglishToSpanishFetcher,
  WebsterLearnersFetcher
}
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext

class EnglishDefinitionService @Inject() (
    val elasticsearch: ElasticsearchCacheClient,
    val websterClient: MirriamWebsterClient,
    override val config: Configuration,
    implicit val ec: ExecutionContext
) extends LanguageDefinitionService[EnglishDefinition] {
  override val wordLanguage: Language = Language.ENGLISH
  override val sources: List[DefinitionSource] =
    List(
      DefinitionSource.MIRRIAM_WEBSTER_LEARNERS,
      DefinitionSource.WIKTIONARY
    )

  // TODO enhance by searching for all versions of stems

  override val definitionFetchers: Map[
    (DefinitionSource, Language),
    DefinitionFetcher[_, EnglishDefinition]
  ] =
    Map(
      (
        DefinitionSource.MIRRIAM_WEBSTER_LEARNERS,
        Language.ENGLISH
      ) -> new WebsterLearnersFetcher(websterClient),
      (
        DefinitionSource.MIRRIAM_WEBSTER_SPANISH,
        Language.SPANISH
      ) -> new WebsterEnglishToSpanishFetcher(websterClient)
    )
}
