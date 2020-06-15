package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.DefinitionDTO

trait Definition {
  val subdefinitions: List[String]
  val tag: String
  val examples: List[String]
  // These fields are needed for elasticsearch lookup
  // But do not need to be presented to the user.
  val definitionLanguage: Language
  val wordLanguage: Language
  val source: DefinitionSource
  val token: String

  // This always needs to know how to convert itself to a DTO
  val toDTO: DefinitionDTO
}

object Definition {
  def apply(subdefinitions: List[String],
            tag: String,
            examples: List[String],
            wordLanguage: Language,
            definitionLanguage: Language,
            source: DefinitionSource,
            token: String): Definition =
    GenericDefinition(
      subdefinitions,
      tag,
      examples,
      wordLanguage,
      definitionLanguage,
      source,
      token
    )
  def definitionListToDefinitionDTOList(
    definitions: Seq[Definition]
  ): Seq[DefinitionDTO] =
    definitions.map(x => x.toDTO)
}
