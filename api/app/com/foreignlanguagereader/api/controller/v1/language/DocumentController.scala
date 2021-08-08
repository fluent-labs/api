package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.error.BadInputException
import io.fluentlabs.content.types.Language
import io.fluentlabs.content.types.Language.{Language, fromString}
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.RequestPath
import com.foreignlanguagereader.domain.service.DocumentService
import io.fluentlabs.dto.v1.document.DocumentRequest
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.libs.{Json => JavaJson}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocumentController @Inject() (
    val controllerComponents: ControllerComponents,
    documentService: DocumentService,
    metrics: MetricsReporter,
    implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  implicit def pathBinder: PathBindable[Language] =
    new PathBindable[Language] {
      override def bind(key: String, value: String): Either[String, Language] =
        fromString(value) match {
          case Some(language) =>
            Right(language)
          case _ => Left(value)
        }
      override def unbind(key: String, value: Language): String = value.toString
    }

  implicit val documentRequestReader: Reads[DocumentRequest] =
    (JsPath \ "text").read[String].map(text => new DocumentRequest(text))

  def document(
      wordLanguage: Language
  ): Action[JsValue] =
    Action.async(parse.json) { request =>
      {
        metrics.reportLearnerLanguage(wordLanguage, Language.UNKNOWN)
        request.body.validate[DocumentRequest] match {
          case JsSuccess(documentRequest: DocumentRequest, _) =>
            documentService
              .getWordsForDocument(
                wordLanguage,
                documentRequest.getText
              )
              .map(words => {
                Ok(JavaJson.stringify(JavaJson.toJson(words.map(_.toDTO))))
              })
          case JsError(errors) =>
            logger.error(
              s"Invalid request body given to document service: $errors"
            )
            metrics.reportBadRequest(RequestPath.DOCUMENT)
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
