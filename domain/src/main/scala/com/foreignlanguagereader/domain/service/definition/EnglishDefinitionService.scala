package com.foreignlanguagereader.domain.service.definition

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.definition.{
  DefinitionSource,
  EnglishDefinition
}
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchCacheClient
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.fetcher.english.{
  WebsterEnglishToSpanishFetcher,
  WebsterLearnersFetcher
}
import com.foreignlanguagereader.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext

class EnglishDefinitionService @Inject() (
    val elasticsearch: ElasticsearchCacheClient,
    val websterLearnersFetcher: WebsterLearnersFetcher,
    val websterEnglishToSpanishFetcher: WebsterEnglishToSpanishFetcher,
    override val config: Configuration,
    val metrics: MetricsReporter,
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
      ) -> websterLearnersFetcher,
      (
        DefinitionSource.MIRRIAM_WEBSTER_SPANISH,
        Language.SPANISH
      ) -> websterEnglishToSpanishFetcher
    )
}
