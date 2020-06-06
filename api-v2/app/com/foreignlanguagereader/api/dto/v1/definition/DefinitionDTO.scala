package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.definition.Definition
import play.api.libs.json.{Format, Json}

class DefinitionDTO(val subdefinitions: List[String],
                    val tag: String,
                    val examples: List[String])

object DefinitionDTO {
  def apply(subdefinitions: List[String],
            tag: String,
            examples: List[String]): DefinitionDTO =
    new DefinitionDTO(subdefinitions, tag, examples)

  def unapply(
    dto: DefinitionDTO
  ): Option[(List[String], String, List[String])] =
    Some(dto.subdefinitions, dto.tag, dto.examples)

  // Mappers
  implicit def definitionToDefinitionDTO(
    definition: Definition
  ): DefinitionDTO =
    DefinitionDTO(
      definition.subdefinitions,
      definition.tag,
      definition.examples
    )

  implicit def definitionListToDefinitionDTOList(
    definitions: List[Definition]
  ): List[DefinitionDTO] =
    definitions.map(x => definitionToDefinitionDTO(x))

  implicit val format: Format[DefinitionDTO] = Json.format
}
