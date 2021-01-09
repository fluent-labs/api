package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import play.api.Logger
import play.api.mvc._
import play.libs.{Json => JavaJson}

import javax.inject._
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
      getDefinitions(wordLanguage, Language.ENGLISH, word)
    }

  def getDefinitions(
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
}
