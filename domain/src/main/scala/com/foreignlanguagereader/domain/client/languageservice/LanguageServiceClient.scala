package com.foreignlanguagereader.domain.client.languageservice

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.{
  PartOfSpeech,
  Word
}
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerResult,
  RestClient,
  RestClientBuilder
}
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.dto.v1.document.DocumentRequest
import com.foreignlanguagereader.dto.v1.health.ReadinessStatus.ReadinessStatus
import play.api.{Configuration, Logger}
import play.libs.{Json => JavaJson}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LanguageServiceClient @Inject() (
    config: Configuration,
    val system: ActorSystem,
    clientBuilder: RestClientBuilder,
    metrics: MetricsReporter
) {
  val logger: Logger = Logger(this.getClass)
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("language-service.context")

  val timeout: FiniteDuration =
    Duration(config.get[Int]("language-service.timeout"), TimeUnit.SECONDS)
  val baseUrl: String = config.get[String]("language-service.url")

  val client: RestClient =
    clientBuilder.buildClient("LanguageServiceClient", timeout = timeout)

  def getWordsForDocument(
      language: Language,
      document: String
  ): Future[CircuitBreakerResult[List[Word]]] = {
    val timer =
      metrics.reportLanguageServiceRequestStarted(language)
    val request =
      JavaJson.stringify(JavaJson.toJson(new DocumentRequest(document)))
    val result = client
      .post[String, List[LanguageServiceWord]](
        s"http://$baseUrl/v1/tagging/${language.toString}/document",
        request,
        e => {
          logger.error(
            s"Failed to get tokens in $language for request: $document",
            e
          )
          metrics.reportLanguageServiceFailure(timer, language)
        }
      )
    metrics.reportLanguageServiceRequestFinished(timer)
    Nested(result)
      .map(
        _.map(word =>
          Word(
            language = language,
            token = word.token,
            tag =
              PartOfSpeech.fromString(word.tag).getOrElse(PartOfSpeech.UNKNOWN),
            lemma = word.lemma,
            definitions = List(),
            gender = None,
            number = None,
            tense = None,
            proper = None,
            processedToken = word.token
          )
        )
      )
      .value
  }

  def health(): ReadinessStatus = client.breaker.health()
}
