package com.foreignlanguagereader.domain.client

import akka.actor.ActorSystem
import com.foreignlanguagereader.content.types.external.definition.webster.{
  WebsterLearnersDefinitionEntry,
  WebsterSpanishDefinitionEntry
}
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerResult,
  RestClient,
  RestClientBuilder
}
import com.foreignlanguagereader.domain.metrics.{Metric, MetricsReporter}
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus.ReadinessStatus
import play.api.libs.json.Reads
import play.api.{Configuration, Logger}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MirriamWebsterClient @Inject() (
    config: Configuration,
    val system: ActorSystem,
    clientBuilder: RestClientBuilder,
    metrics: MetricsReporter
) {
  val logger: Logger = Logger(this.getClass)
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("webster-context")
  val timeout: FiniteDuration =
    Duration(config.get[Int]("webster.timeout"), TimeUnit.SECONDS)
  val client: RestClient =
    clientBuilder.buildClient("WebsterClient", timeout = timeout)

  logger.info(s"Initialized webster client with a $timeout second timeout")

  val learnersApiKey: String = config.get[String]("webster.learners")
  val spanishApiKey: String = config.get[String]("webster.spanish")

  implicit val readsListLearners: Reads[List[WebsterLearnersDefinitionEntry]] =
    WebsterLearnersDefinitionEntry.helper.readsList
  implicit val readsListSpanish: Reads[List[WebsterSpanishDefinitionEntry]] =
    WebsterSpanishDefinitionEntry.helper.readsList

  // TODO: Make definition not found not be an error that increments the circuit breaker.
  // That means the input is bad, not the connection to the service.

  // TODO filter garbage

  val learnersLabel = "learners"
  def getLearnersDefinition(
      word: Word
  ): Future[CircuitBreakerResult[List[WebsterLearnersDefinitionEntry]]] =
    client
      .get[List[WebsterLearnersDefinitionEntry]](
        s"https://www.dictionaryapi.com/api/v3/references/learners/json/${word.processedToken}?key=$learnersApiKey",
        () => metrics.report(Metric.WEBSTER_SUCCESS, learnersLabel),
        () => metrics.report(Metric.WEBSTER_FAILURE, learnersLabel)
      )

  val spanishLabel = "spanish"
  def getSpanishDefinition(
      word: Word
  ): Future[CircuitBreakerResult[List[WebsterSpanishDefinitionEntry]]] =
    client
      .get[List[WebsterSpanishDefinitionEntry]](
        s"https://www.dictionaryapi.com/api/v3/references/spanish/json/${word.processedToken}?key=$spanishApiKey",
        () => metrics.report(Metric.WEBSTER_SUCCESS, spanishLabel),
        () => metrics.report(Metric.WEBSTER_FAILURE, spanishLabel)
      )

  def health(): ReadinessStatus = client.breaker.health()
}
