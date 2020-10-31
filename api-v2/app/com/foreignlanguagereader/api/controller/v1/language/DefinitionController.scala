package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.word.Word
import com.foreignlanguagereader.api.service.definition.DefinitionService
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.Definition
import javax.inject._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionController @Inject() (
    val controllerComponents: ControllerComponents,
    val definitionService: DefinitionService,
    implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def definition(wordLanguage: Language, word: String): Action[AnyContent] =
    Action.async {
      getDefinitions(wordLanguage, Language.ENGLISH, word)
    }

  def definitionIn(
      wordLanguage: Language,
      definitionLanguage: Language,
      word: String
  ): Action[AnyContent] =
    Action.async {
      getDefinitions(wordLanguage, definitionLanguage, word)
    }

  def getDefinitions(
      wordLanguage: Language,
      definitionLanguage: Language,
      word: String
  ): Future[Result] = {
    definitionService
      .getDefinition(
        wordLanguage,
        definitionLanguage,
        Word.fromToken(word, wordLanguage)
      )
      .map {
        case List() =>
          NotFound(s"Definition for $word in $wordLanguage not found")
        case definitions =>
          Ok(
            Json
              .toJson(Definition.definitionListToDefinitionDTOList(definitions))
          )
      }
      .recover {
        case error: Throwable =>
          logger.error(
            s"Failed to get definitions for $word in $wordLanguage: ${error.getMessage}",
            error
          )
          InternalServerError(
            s"Failed to get definitions for $word in $wordLanguage"
          )
      }
  }
}
