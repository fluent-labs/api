package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.metrics.{Metric, MetricsReporter}
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
  val definitionLabel = "definitions"

  def definition(wordLanguage: Language, word: String): Action[AnyContent] =
    Action.async {
      getDefinitions(wordLanguage, Language.ENGLISH, word)
    }

  def getDefinitions(
      wordLanguage: Language,
      definitionLanguage: Language,
      word: String
  ): Future[Result] = {
    metrics.timeRequest(definitionLabel)(() => {
      metrics.inc(Metric.ACTIVE_REQUESTS)
      metrics.report(Metric.REQUEST_COUNT, definitionLabel)
      metrics.reportLanguageUsage(wordLanguage, definitionLanguage)
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
        .recover {
          case error: Throwable =>
            metrics.report(Metric.REQUEST_FAILURES, definitionLabel)
            logger.error(
              s"Failed to get definitions for $word in $wordLanguage: ${error.getMessage}",
              error
            )
            InternalServerError(
              s"Failed to get definitions for $word in $wordLanguage"
            )
        }
    })
  }.map(r => {
    metrics.dec(Metric.ACTIVE_REQUESTS)
    r
  })
}
