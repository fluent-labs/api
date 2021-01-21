package com.foreignlanguagereader.domain.fetcher.spanish

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.SpanishDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WiktionarySpanishFetcher @Inject() (val metrics: MetricsReporter)
    extends DefinitionFetcher[
      WiktionaryDefinitionEntry,
      SpanishDefinition
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
  ): SpanishDefinition = DefinitionEntry.buildSpanishDefinition(entry, tag)

  override implicit val reads: Reads[WiktionaryDefinitionEntry] =
    WiktionaryDefinitionEntry.format
  override implicit val writes: Writes[WiktionaryDefinitionEntry] =
    WiktionaryDefinitionEntry.format
}
