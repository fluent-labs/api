package com.foreignlanguagereader.domain.fetcher.english

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.EnglishDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WiktionaryEnglishFetcher()(
    override implicit val ec: ExecutionContext
) extends DefinitionFetcher[
      WiktionaryDefinitionEntry,
      EnglishDefinition
    ] {
  override def fetch(
      language: Language,
      word: Word
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
  ): EnglishDefinition = DefinitionEntry.buildEnglishDefinition(entry, tag)

  override implicit val reads: Reads[WiktionaryDefinitionEntry] =
    WiktionaryDefinitionEntry.format
  override implicit val writes: Writes[WiktionaryDefinitionEntry] =
    WiktionaryDefinitionEntry.format
}
