package com.foreignlanguagereader.api.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionEntry
import com.foreignlanguagereader.api.dto.v1.ReadinessStatus.ReadinessStatus
import com.foreignlanguagereader.api.dto.v1.{ReadinessStatus, Word}
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class LanguageServiceClient @Inject()(config: Configuration,
                                      val ws: WSClient,
                                      val system: ActorSystem) {
  val logger: Logger = Logger(this.getClass)

  implicit val myExecutionContext: ExecutionContext =
    system.dispatchers.lookup("language-service-context")

  val languageServiceBaseUrl: String =
    config.get[String]("language-service.url")
  val languageServiceTimeout =
    Duration(config.get[Int]("language-service.timeout"), TimeUnit.SECONDS)

  // This token only works for localhost
  // Will need to replace this with config properties when I learn how secrets work in play
  val languageServiceAuthToken = "simpletoken"

  // Used for the health check. Just makes sure we can connect to language service
  def checkConnection(timeout: Duration): Future[ReadinessStatus] = {
    ws.url(s"$languageServiceBaseUrl/health")
      .withRequestTimeout(timeout)
      .get()
      .map(
        result =>
          result.status match {
            case OK => ReadinessStatus.UP
            case _ =>
              logger
                .error(s"Error connecting to language service: ${result.body}")
              ReadinessStatus.DOWN
        }
      )
      .recover {
        case e: Exception =>
          logger
            .error(s"Failed to connect to language service: ${e.getMessage}", e)
          ReadinessStatus.DOWN
      }
  }

  def getDefinition(wordLanguage: Language,
                    _definitionLanguage: Language,
                    word: String): Future[Option[Seq[DefinitionEntry]]] = {
    ws.url(s"$languageServiceBaseUrl/v1/definition/$language/$word")
      .withRequestTimeout(languageServiceTimeout)
      .withHttpHeaders(("Authorization", languageServiceAuthToken))
      .get()
      .map(
        response =>
          response.json.validate[Seq[DefinitionEntry]] match {
            case JsSuccess(result, _) => Some(result)
            case JsError(errors) =>
              logger.error(
                s"Failed to parse definition in $language for word $word from language service: $errors"
              )
              None
        }
      )
      .recover {
        case e: Exception =>
          logger.error(
            s"Failed to get definitions in $language for word $word from language service: ${e.getMessage}",
            e
          )
          throw e
      }
  }

  def getWordsForDocument(documentLanguage: Language,
                          document: String): Future[Seq[Word]] = {
    ws.url(s"$languageServiceBaseUrl/v1/tagging/$documentLanguage/document")
      .withRequestTimeout(languageServiceTimeout)
      .withHttpHeaders(("Authorization", languageServiceAuthToken))
      .post(document)
      .map(
        response =>
          response.json.validate[Seq[Word]] match {
            case JsSuccess(result, _) => result
            case JsError(errors) =>
              logger.error(
                s"Failed to parse words in $language for document $document from language service: $errors"
              )
              throw new IllegalArgumentException(
                s"Failed to parse words in $language for document $document from language service: $errors"
              )
        }
      )
      .recover {
        case e: Exception =>
          logger.error(
            s"Failed to get words in $language for document $document from language service: ${e.getMessage}",
            e
          )
          throw e
      }
  }
}
