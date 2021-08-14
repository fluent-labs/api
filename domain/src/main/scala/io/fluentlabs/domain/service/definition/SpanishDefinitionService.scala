package io.fluentlabs.domain.service.definition

import io.fluentlabs.content.types.Language
import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.internal.definition.DefinitionSource.DefinitionSource
import io.fluentlabs.content.types.internal.definition.{
  DefinitionSource,
  SpanishDefinition
}
import io.fluentlabs.domain.client.elasticsearch.ElasticsearchCacheClient
import io.fluentlabs.domain.fetcher.DefinitionFetcher
import io.fluentlabs.domain.fetcher.spanish.WebsterSpanishToEnglishFetcher
import io.fluentlabs.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext

class SpanishDefinitionService @Inject() (
    val elasticsearch: ElasticsearchCacheClient,
    val websterSpanishToEnglishFetcher: WebsterSpanishToEnglishFetcher,
    override val config: Configuration,
    val metrics: MetricsReporter,
    implicit val ec: ExecutionContext
) extends LanguageDefinitionService[SpanishDefinition] {
  override val wordLanguage: Language = Language.SPANISH
  override val sources: List[DefinitionSource] =
    List(DefinitionSource.MIRRIAM_WEBSTER_SPANISH, DefinitionSource.WIKTIONARY)

  override val definitionFetchers: Map[
    (DefinitionSource, Language),
    DefinitionFetcher[_, SpanishDefinition]
  ] = Map(
    (
      DefinitionSource.MIRRIAM_WEBSTER_SPANISH,
      Language.ENGLISH
    ) -> websterSpanishToEnglishFetcher
  )

}
