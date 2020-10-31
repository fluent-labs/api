package com.foreignlanguagereader.dto.v1.definition

import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO.PartOfSpeechDTO
import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{ObjectTypeDescription, deriveObjectType}
import sangria.schema.ObjectType

case class GenericDefinitionDTO(
    id: String,
    subdefinitions: List[String],
    tag: PartOfSpeechDTO,
    examples: Option[List[String]]
) extends DefinitionDTO
object GenericDefinitionDTO {
  implicit val format: Format[GenericDefinitionDTO] = Json.format

  implicit val graphQlType: ObjectType[Unit, GenericDefinitionDTO] =
    deriveObjectType[Unit, GenericDefinitionDTO](
      ObjectTypeDescription("A definition for a word")
    )
}
