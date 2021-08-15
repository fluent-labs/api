package io.fluentlabs.domain.fetcher.english

import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.external.definition.DefinitionEntry
import io.fluentlabs.content.types.external.definition.webster.WebsterLearnersDefinitionEntry
import io.fluentlabs.content.types.internal.definition.EnglishDefinition
import io.fluentlabs.content.types.internal.word.PartOfSpeech.PartOfSpeech
import io.fluentlabs.content.types.internal.word.Word
import io.fluentlabs.domain.client.MirriamWebsterClient
import io.fluentlabs.domain.client.circuitbreaker.CircuitBreakerResult
import io.fluentlabs.domain.fetcher.DefinitionFetcher
import io.fluentlabs.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WebsterLearnersFetcher @Inject() (
    websterClient: MirriamWebsterClient,
    val metrics: MetricsReporter
) extends DefinitionFetcher[
      WebsterLearnersDefinitionEntry,
      EnglishDefinition
    ] {
  override def fetch(
      language: Language,
      word: Word
  )(implicit
      ec: ExecutionContext
  ): Future[CircuitBreakerResult[List[WebsterLearnersDefinitionEntry]]] =
    websterClient.getLearnersDefinition(word)

  override def convertToDefinition(
      entry: WebsterLearnersDefinitionEntry,
      tag: PartOfSpeech
  ): EnglishDefinition = DefinitionEntry.buildEnglishDefinition(entry, tag)

  override implicit val reads: Reads[WebsterLearnersDefinitionEntry] =
    WebsterLearnersDefinitionEntry.reads
  override implicit val writes: Writes[WebsterLearnersDefinitionEntry] =
    WebsterLearnersDefinitionEntry.writes
}
