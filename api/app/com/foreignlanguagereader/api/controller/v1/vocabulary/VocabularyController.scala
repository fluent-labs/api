package com.foreignlanguagereader.api.controller.v1.vocabulary

import com.foreignlanguagereader.domain.service.VocabularyService
import play.api.Logger
import play.api.mvc._
import play.libs.{Json => JavaJson}

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class VocabularyController @Inject() (
    val controllerComponents: ControllerComponents,
    vocabularyService: VocabularyService,
    implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def getAllWords: Action[AnyContent] =
    Action.async {
      logger.info("Getting all words")
      vocabularyService.getAllWords.map(words =>
        Ok(JavaJson.stringify(JavaJson.toJson(words.map(_.toDTO))))
      )
    }
}
