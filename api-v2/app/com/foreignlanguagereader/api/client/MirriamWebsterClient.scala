package com.foreignlanguagereader.api.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.domain.definition.entry.webster.WebsterLearnersDefinitionEntry
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

class MirriamWebsterClient @Inject()(config: Configuration,
                                     val ws: WSClient,
                                     val system: ActorSystem) {
  val logger: Logger = Logger(this.getClass)

  implicit val myExecutionContext: ExecutionContext =
    system.dispatchers.lookup("webster-context")


  val languageServiceTimeout =
    Duration(config.get[Int]("webster.timeout"), TimeUnit.SECONDS)

  def getLearnersDefinition(
    word: String
  ): Future[Option[WebsterLearnersDefinitionEntry]] =
    ws.url(
        s"https://www.dictionaryapi.com/api/v3/references/learners/json/$word?key=$learnersApiKey"
      )
      .withRequestTimeout(languageServiceTimeout)
      .get()
      .map(response => {
        response.json.validate[WebsterLearnersDefinitionEntry] match {
          case JsSuccess(result, _) => Some(result)
          case JsError(errors) =>
            logger.error(
              s"Failed to parse definition for word $word from mirriam webster: $errors"
            )
            throw new IllegalStateException(
              s"Failed to get definitions for word $word"
            )
        }
      })
      .recover {
        case e: Exception =>
          logger.error(
            s"Failed to get definitions for word $word from Mirriam Webster: ${e.getMessage}",
            e
          )
          throw e
      }
}
