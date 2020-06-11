package com.foreignlanguagereader.api.dto.v1.definition

import play.api.libs.json.{Format, Json}

case class GenericDefinitionDTO(subdefinitions: List[String],
                                tag: String,
                                examples: List[String])
    extends DefinitionDTO
object GenericDefinitionDTO {
  implicit val format: Format[GenericDefinitionDTO] = Json.format
}
