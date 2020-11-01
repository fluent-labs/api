package com.foreignlanguagereader.domain.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.content.types.external.definition.webster.webster.{
  WebsterLearnersDefinitionEntry,
  WebsterSpanishDefinitionEntry
}
import com.foreignlanguagereader.content.types.internal.definition.Definition
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerResult,
  Circuitbreaker,
  WsClient
}
import javax.inject.Inject
import play.api.libs.json.Reads
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class MirriamWebsterClient @Inject() (
    config: Configuration,
    val ws: WSClient,
    val system: ActorSystem
) extends WsClient
    with Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("webster-context")
  override val timeout: FiniteDuration =
    Duration(config.get[Int]("webster.timeout"), TimeUnit.SECONDS)

  val learnersApiKey = ""
  val spanishApiKey = ""

  implicit val readsListLearners: Reads[List[WebsterLearnersDefinitionEntry]] =
    WebsterLearnersDefinitionEntry.helper.readsList
  implicit val readsListSpanish: Reads[List[WebsterSpanishDefinitionEntry]] =
    WebsterSpanishDefinitionEntry.helper.readsList

  // TODO: Make definition not found not be an error that increments the circuit breaker.
  // That means the input is bad, not the connection to the service.

  // TODO filter garbage

  def getLearnersDefinition(
      word: Word
  ): Nested[Future, CircuitBreakerResult, List[Definition]] =
    get[List[WebsterLearnersDefinitionEntry]](
      s"https://www.dictionaryapi.com/api/v3/references/learners/json/${word.processedToken}?key=$learnersApiKey"
    ).map(results => results.map(_.toDefinition(word.tag)))

  def getSpanishDefinition(
      word: Word
  ): Nested[Future, CircuitBreakerResult, List[Definition]] =
    get[List[WebsterSpanishDefinitionEntry]](
      s"https://www.dictionaryapi.com/api/v3/references/spanish/json/${word.processedToken}?key=$spanishApiKey"
    ).map(results => results.map(_.toDefinition(word.tag)))
}
