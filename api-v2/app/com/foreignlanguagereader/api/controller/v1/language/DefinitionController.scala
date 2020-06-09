package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.dto.v1.definition.{
  ChineseDefinitionDTO,
  DefinitionDTO,
  GenericDefinitionDTO
}
import com.foreignlanguagereader.api.service.DefinitionService
import javax.inject._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class DefinitionController @Inject()(
  val controllerComponents: ControllerComponents,
  val definitionService: DefinitionService,
  implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  def definition(wordLanguage: Language,
                 definitionLanguage: Language,
                 word: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      definitionService
        .getDefinition(wordLanguage, definitionLanguage, word)
        .map {
          case None =>
            NotFound(s"Definition for $word in $language not found")
          case Some(definitions) => Ok(serializeDefinitionDTO(definitions))
        }
        .recover(error => {
          val message =
            s"Failed to get definitions for $word in $language: ${error.getMessage}"
          logger.error(message, error)
          InternalServerError(message)
        })
  }

  // This exists so that the controller can properly serialize DTOs.
  // Each DTO has a serializer, but the abstract parent class doesn't.
  // Without this, the compiler can't guarantee the serializer method exists.
  // Ugly but better than making many controllers and routes.
  def serializeDefinitionDTO(dto: Seq[DefinitionDTO]): JsValue = {
    dto match {
      case g: List[GenericDefinitionDTO] => Json.toJson(g)
      case c: List[ChineseDefinitionDTO] => Json.toJson(c)
    }
  }
}
