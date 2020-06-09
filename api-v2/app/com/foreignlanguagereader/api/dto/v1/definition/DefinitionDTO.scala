package com.foreignlanguagereader.api.dto.v1.definition

import play.api.libs.json.{Format, Json}

trait DefinitionDTO {
  val subdefinitions: List[String]
  val tag: String
  val examples: List[String]
}
object DefinitionDTO {
  implicit val format: Format[GenericDefinitionDTO] = Json.format

  // Default to generic
  def apply(subdefinitions: List[String], tag: String, examples: List[String]) =
    GenericDefinitionDTO(subdefinitions, tag, examples)
}
