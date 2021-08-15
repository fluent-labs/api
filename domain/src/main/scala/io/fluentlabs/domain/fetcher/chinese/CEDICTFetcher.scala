package io.fluentlabs.domain.fetcher.chinese

import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.external.definition.DefinitionEntry
import io.fluentlabs.content.types.external.definition.cedict.CEDICTDefinitionEntry
import io.fluentlabs.content.types.internal.definition.ChineseDefinition
import io.fluentlabs.content.types.internal.word.PartOfSpeech.PartOfSpeech
import io.fluentlabs.content.types.internal.word.Word
import io.fluentlabs.domain.client.circuitbreaker.{
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import io.fluentlabs.domain.fetcher.DefinitionFetcher
import io.fluentlabs.domain.metrics.MetricsReporter
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
