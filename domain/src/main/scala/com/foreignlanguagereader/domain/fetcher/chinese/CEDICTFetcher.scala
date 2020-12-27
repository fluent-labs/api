package com.foreignlanguagereader.domain.fetcher.chinese

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.ChineseDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.repository.definition.Cedict
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class CEDICTFetcher(override implicit val ec: ExecutionContext)
    extends DefinitionFetcher[CEDICTDefinitionEntry, ChineseDefinition] {
  override def fetch(
      language: Language,
      word: Word
  ): Future[CircuitBreakerResult[List[CEDICTDefinitionEntry]]] =
    Cedict.getDefinition(word) match {
      case Some(entries) => Future.successful(CircuitBreakerAttempt(entries))
      case None =>
        Future.successful(
          CircuitBreakerNonAttempt[List[CEDICTDefinitionEntry]]()
        )
    }

  override def convertToDefinition(
      entry: CEDICTDefinitionEntry,
      tag: PartOfSpeech
  ): ChineseDefinition = DefinitionEntry.buildChineseDefinition(entry, tag)

  override implicit val reads: Reads[CEDICTDefinitionEntry] =
    CEDICTDefinitionEntry.format
  override implicit val writes: Writes[CEDICTDefinitionEntry] =
    CEDICTDefinitionEntry.format
}
