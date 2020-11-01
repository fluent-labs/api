package com.foreignlanguagereader.api.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerResult,
  Circuitbreaker,
  WsClient
}
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.external.definition.DefinitionEntry
import com.foreignlanguagereader.domain.internal.definition.Definition
import com.foreignlanguagereader.domain.internal.word.Word
import javax.inject.Inject
import play.api.libs.json.Reads
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class LanguageServiceClient @Inject() (
    config: Configuration,
    val ws: WSClient,
    val system: ActorSystem
) extends WsClient
    with Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("language-service-context")
  override val timeout =
    Duration(config.get[Int]("language-service.timeout"), TimeUnit.SECONDS)

  // This token only works for localhost
  // Will need to replace this with config properties when I learn how secrets work in play
  val languageServiceAuthToken = "simpletoken"
  override val headers: List[(String, String)] = List(
    ("Authorization,", languageServiceAuthToken)
  )

  val languageServiceBaseUrl: String =
    config.get[String]("language-service.url")

  implicit val readsDefinitionEntry: Reads[List[DefinitionEntry]] =
    DefinitionEntry.readsList

  def getDefinition(
      wordLanguage: Language,
      word: Word
  ): Nested[Future, CircuitBreakerResult, List[Definition]] =
    get(
      s"$languageServiceBaseUrl/v1/definition/$wordLanguage/${word.processedToken}"
    ).map(a => a.map(_.toDefinition(word.tag)))
}
