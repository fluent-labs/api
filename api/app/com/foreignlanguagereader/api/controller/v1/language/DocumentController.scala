package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.dto.v1.document.DocumentRequest
import com.foreignlanguagereader.api.service.DocumentService
import javax.inject._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocumentController @Inject() (
    val controllerComponents: ControllerComponents,
    val documentService: DocumentService,
    implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def definition(
      wordLanguage: Language,
      definitionLanguage: Language
  ): Action[JsValue] =
    Action.async(parse.json) { request =>
      request.body.validate[DocumentRequest] match {
        case JsSuccess(documentRequest: DocumentRequest, _) =>
          documentService
            .getWordsForDocument(
              wordLanguage,
              definitionLanguage,
              documentRequest.text
            )
            .map {
              case List() => NoContent
              case words  => Ok(Json.toJson(words.map(_.toDTO)))
            }
            .recover {
              case error: Throwable =>
                val message =
                  s"Failed to get words in $wordLanguage: ${error.getMessage}"
                logger.error(message, error)
                InternalServerError(message)
            }
        case JsError(errors) =>
          logger.error(
            s"Invalid request body given to document service: $errors"
          )
          Future {
            BadRequest("Invalid request body, please try again")
          }
      }
    }
}
