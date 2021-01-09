package com.foreignlanguagereader.domain.fetcher.chinese

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.ChineseDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import play.api.Logger
import play.api.libs.json.{Reads, Writes}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CEDICTFetcher @Inject() (val metrics: MetricsReporter)
    extends DefinitionFetcher[CEDICTDefinitionEntry, ChineseDefinition] {
  override val logger: Logger = Logger(this.getClass)

  override def fetch(
      language: Language,
      word: Word
  )(implicit
      ec: ExecutionContext
  ): Future[CircuitBreakerResult[List[CEDICTDefinitionEntry]]] =
    Future.apply(
      CircuitBreakerFailedAttempt(
        new NotImplementedError(
          "CEDICT is a elasticsearch only fetcher"
        )
      )
    )

  override def convertToDefinition(
      entry: CEDICTDefinitionEntry,
      tag: PartOfSpeech
  ): ChineseDefinition = {
    logger.info(s"Converting to definition: $entry")
    DefinitionEntry.buildChineseDefinition(entry, tag)
  }

  override implicit val reads: Reads[CEDICTDefinitionEntry] =
    CEDICTDefinitionEntry.format
  override implicit val writes: Writes[CEDICTDefinitionEntry] =
    CEDICTDefinitionEntry.format
}
