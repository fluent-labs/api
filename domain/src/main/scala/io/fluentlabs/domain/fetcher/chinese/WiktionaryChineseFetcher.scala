package io.fluentlabs.domain.fetcher.chinese

import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.external.definition.DefinitionEntry
import io.fluentlabs.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import io.fluentlabs.content.types.internal.definition.ChineseDefinition
import io.fluentlabs.content.types.internal.word.PartOfSpeech.PartOfSpeech
import io.fluentlabs.content.types.internal.word.Word
import io.fluentlabs.domain.client.circuitbreaker.{
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import io.fluentlabs.domain.fetcher.DefinitionFetcher
import io.fluentlabs.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WiktionaryChineseFetcher @Inject() (val metrics: MetricsReporter)
    extends DefinitionFetcher[
      WiktionaryDefinitionEntry,
      ChineseDefinition
    ] {
  override def fetch(
      language: Language,
      word: Word
  )(implicit
      ec: ExecutionContext
  ): Future[CircuitBreakerResult[List[WiktionaryDefinitionEntry]]] =
    Future.apply(
      CircuitBreakerFailedAttempt(
        new NotImplementedError(
          "Wiktionary is a elasticsearch only fetcher"
        )
      )
    )

  override def convertToDefinition(
      entry: WiktionaryDefinitionEntry,
      tag: PartOfSpeech
  ): ChineseDefinition = DefinitionEntry.buildChineseDefinition(entry, tag)

  override implicit val reads: Reads[WiktionaryDefinitionEntry] =
    WiktionaryDefinitionEntry.format
  override implicit val writes: Writes[WiktionaryDefinitionEntry] =
    WiktionaryDefinitionEntry.format
}
