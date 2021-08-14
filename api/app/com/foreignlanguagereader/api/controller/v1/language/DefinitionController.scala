package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.error.BadInputException
import io.fluentlabs.content.types.Language
import io.fluentlabs.content.types.Language.{Language, fromString}
import io.fluentlabs.content.types.internal.word.Word
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.RequestPath
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import io.fluentlabs.dto.v1.definition.DefinitionsRequest
import play.api.Logger
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsValue, Reads}
import play.api.mvc._
import play.libs.{Json => JavaJson}

import javax.inject._
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionController @Inject() (
    val controllerComponents: ControllerComponents,
    definitionService: DefinitionService,
    metrics: MetricsReporter,
    implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def definition(wordLanguage: Language, word: String): Action[AnyContent] =
    Action.async {
      getDefinition(wordLanguage, Language.ENGLISH, word)
    }

  def getDefinition(
      wordLanguage: Language,
      definitionLanguage: Language,
      word: String
  ): Future[Result] = {
    metrics.reportLearnerLanguage(wordLanguage, definitionLanguage)
    logger.info(
      s"Getting definitions in $definitionLanguage for $wordLanguage word $word"
    )

    definitionService
      .getDefinition(
        wordLanguage,
        definitionLanguage,
        Word.fromToken(word, wordLanguage)
      )
      .map { definitions =>
        {
          Ok(
            JavaJson.stringify(
              JavaJson
                .toJson(definitions.map(_.toDTO))
            )
          )
        }
      }
  }

  implicit val definitionsRequestReader: Reads[DefinitionsRequest] =
    (JsPath \ "words")
      .read[List[String]]
      .map(words => new DefinitionsRequest(words.asJava))

  def definitions(
      wordLanguage: Language,
      definitionLanguage: Language
  ): Action[JsValue] =
    Action.async(parse.json) { request =>
      {
        metrics.reportLearnerLanguage(wordLanguage, definitionLanguage)
        request.body.validate[DefinitionsRequest] match {
          case JsSuccess(definitionsRequest: DefinitionsRequest, _) =>
            definitionService
              .getDefinitions(
                wordLanguage,
                definitionLanguage,
                definitionsRequest.getWords.asScala.toList
                  .map(token => Word.fromToken(token, wordLanguage))
              )
              .map(definitions => {
                Ok(
                  JavaJson
                    .stringify(
                      JavaJson.toJson(definitions.mapValues(_.map(_.toDTO)))
                    )
                )
              })
          case JsError(errors) =>
            logger.error(
              s"Invalid request body given to definitions service: $errors"
            )
            metrics.reportBadRequest(RequestPath.DEFINITIONS)
            Future {
              BadRequest(
                JavaJson.stringify(
                  JavaJson.toJson(
                    new BadInputException(
                      "Invalid request body, please try again"
                    )
                  )
                )
              )
            }
        }
      }
    }
}
