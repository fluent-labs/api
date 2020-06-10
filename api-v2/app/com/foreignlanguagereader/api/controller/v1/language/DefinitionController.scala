package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.service.DefinitionService
import javax.inject._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionController @Inject()(
  val controllerComponents: ControllerComponents,
  val definitionService: DefinitionService,
  implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def definition(wordLanguage: Language, word: String): Action[AnyContent] =
    Action.async {
      getDefinitions(wordLanguage, Language.ENGLISH, word)
    }

  def definitionIn(wordLanguage: Language,
                   definitionLanguage: Language,
                   word: String): Action[AnyContent] = Action.async {
    getDefinitions(wordLanguage, definitionLanguage, word)
  }

  def getDefinitions(wordLanguage: Language,
                     definitionLanguage: Language,
                     word: String): Future[Result] = {
    definitionService
      .getDefinition(wordLanguage, definitionLanguage, word)
      .map {
        case None =>
          NotFound(s"Definition for $word in $wordLanguage not found")
        case Some(definitions) =>
          Ok(
            Json
              .toJson(Definition.definitionListToDefinitionDTOList(definitions))
          )
      }
      .recover(error => {
        val message =
          s"Failed to get definitions for $word in $wordLanguage: ${error.getMessage}"
        logger.error(message, error)
        InternalServerError(message)
      })
  }
}
