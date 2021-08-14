package io.fluentlabs.domain.fetcher.spanish

import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.external.definition.DefinitionEntry
import io.fluentlabs.content.types.external.definition.webster.WebsterSpanishDefinitionEntry
import io.fluentlabs.content.types.internal.definition.SpanishDefinition
import io.fluentlabs.content.types.internal.word.PartOfSpeech.PartOfSpeech
import io.fluentlabs.content.types.internal.word.Word
import io.fluentlabs.domain.client.MirriamWebsterClient
import io.fluentlabs.domain.client.circuitbreaker.CircuitBreakerResult
import io.fluentlabs.domain.fetcher.DefinitionFetcher
import io.fluentlabs.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WebsterSpanishToEnglishFetcher @Inject() (
    websterClient: MirriamWebsterClient,
    val metrics: MetricsReporter
) extends DefinitionFetcher[
      WebsterSpanishDefinitionEntry,
      SpanishDefinition
    ] {
  override def fetch(
      language: Language,
      word: Word
  )(implicit
      ec: ExecutionContext
  ): Future[CircuitBreakerResult[List[WebsterSpanishDefinitionEntry]]] =
    websterClient.getSpanishDefinition(word)

  override def convertToDefinition(
      entry: WebsterSpanishDefinitionEntry,
      tag: PartOfSpeech
  ): SpanishDefinition = DefinitionEntry.buildSpanishDefinition(entry, tag)

  override implicit val reads: Reads[WebsterSpanishDefinitionEntry] =
    WebsterSpanishDefinitionEntry.reads
  override implicit val writes: Writes[WebsterSpanishDefinitionEntry] =
    WebsterSpanishDefinitionEntry.writes
}
