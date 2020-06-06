package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.dto.v1.definition.{
  ChineseDefinitionDTO,
  DefinitionDTO,
  GenericDefinitionDTO
}
import com.foreignlanguagereader.api.service.DefinitionService
import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

@Singleton
class DefinitionController @Inject()(
  val controllerComponents: ControllerComponents,
  val definitionService: DefinitionService
) extends BaseController {
  def definition(wordLanguage: Language,
                 definitionLanguage: Language,
                 word: String): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      definitionService
        .getDefinition(wordLanguage, definitionLanguage, word) match {
        case None              => NotFound(s"Definition for $word in $language not found")
        case Some(definitions) => Ok(serializeDefinitionDTO(definitions))
      }
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
