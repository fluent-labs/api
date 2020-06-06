package com.foreignlanguagereader.api.controller.v1.language

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.service.DefinitionService
import javax.inject._
import play.api.libs.json.Json
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
        case Nil         => NotFound(s"Definition for $word in $language not found")
        case definitions => Ok(Json.toJson(definitions))
      }
  }
}
