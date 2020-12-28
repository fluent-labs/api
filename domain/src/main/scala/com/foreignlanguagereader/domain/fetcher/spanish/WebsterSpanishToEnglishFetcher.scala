package com.foreignlanguagereader.domain.fetcher.spanish

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.webster.WebsterSpanishDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.SpanishDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.common.CircuitBreakerResult
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WebsterSpanishToEnglishFetcher(websterClient: MirriamWebsterClient)
    extends DefinitionFetcher[
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
