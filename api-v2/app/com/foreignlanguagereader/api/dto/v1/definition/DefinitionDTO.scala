package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition
}

abstract class DefinitionDTO(subdefinitions: List[String],
                             tag: String,
                             examples: List[String])

// Pretty much just mappers
object DefinitionDTO {
  // Definition => GenericDefinitionDTO
  // We should not map to DefinitionDTO since it is abstract
  implicit def definitionToDefinitionDTO(
    definition: Definition
  ): GenericDefinitionDTO =
    GenericDefinitionDTO(
      definition.subdefinitions,
      definition.tag,
      definition.examples
    )
  implicit def definitionListToDefinitionDTOList(
    definitions: Seq[Definition]
  ): Seq[GenericDefinitionDTO] =
    definitions.map(x => definitionToDefinitionDTO(x))
  implicit def definitionListOptionalToDefinitionDTOList(
    definitions: Option[Seq[Definition]]
  ): Option[Seq[DefinitionDTO]] = definitions match {
    case Some(definitionList) =>
      Some(definitionListToDefinitionDTOList(definitionList))
    case None => None
  }

  // Mappers for ChineseDefinition => ChineseDefinitionDTO
  implicit def chineseDefinitionToDefinitionDTO(
    definition: ChineseDefinition
  ): ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      definition.subdefinitions,
      definition.tag,
      definition.examples,
      definition.pinyin,
      definition.simplified,
      definition.traditional,
      definition.hsk
    )
  implicit def chineseDefinitionListToChineseDefinitionDTOList(
    definitions: Seq[ChineseDefinition]
  ): Seq[ChineseDefinitionDTO] =
    definitions.map(x => chineseDefinitionToDefinitionDTO(x))
  implicit def chineseDefinitionListOptionalToChineseDefinitionDTOListOptional(
    definitions: Option[Seq[ChineseDefinition]]
  ): Option[Seq[ChineseDefinitionDTO]] = definitions match {
    case Some(d) => Some(chineseDefinitionListToChineseDefinitionDTOList(d))
    case None    => None
  }
}
