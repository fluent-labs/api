package com.foreignlanguagereader.domain.fetcher.english

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.webster.WebsterSpanishDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.EnglishDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.circuitbreaker.CircuitBreakerResult
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WebsterEnglishToSpanishFetcher @Inject() (
    websterClient: MirriamWebsterClient,
    val metrics: MetricsReporter
) extends DefinitionFetcher[
      WebsterSpanishDefinitionEntry,
      EnglishDefinition
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
  ): EnglishDefinition = DefinitionEntry.buildEnglishDefinition(entry, tag)

  override implicit val reads: Reads[WebsterSpanishDefinitionEntry] =
    WebsterSpanishDefinitionEntry.reads
  override implicit val writes: Writes[WebsterSpanishDefinitionEntry] =
    WebsterSpanishDefinitionEntry.writes
}
