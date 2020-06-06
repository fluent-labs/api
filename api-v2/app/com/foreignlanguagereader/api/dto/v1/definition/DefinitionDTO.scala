package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.definition.Definition

abstract class DefinitionDTO(subdefinitions: List[String],
                             tag: String,
                             examples: List[String])

// If anything tries to map to this class, send them to GenericDefinitionDTO instead
object DefinitionDTO {
  // Mappers
  implicit def definitionToDefinitionDTO(
    definition: Definition
  ): GenericDefinitionDTO =
    GenericDefinitionDTO(
      definition.subdefinitions,
      definition.tag,
      definition.examples
    )
  implicit def definitionListToDefinitionDTOList(
    definitions: List[Definition]
  ): List[GenericDefinitionDTO] =
    definitions.map(x => definitionToDefinitionDTO(x))
}
