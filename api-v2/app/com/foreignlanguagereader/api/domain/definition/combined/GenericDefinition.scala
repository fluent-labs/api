package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.{
  DefinitionDTO,
  GenericDefinitionDTO
}

case class GenericDefinition(subdefinitions: List[String],
                             tag: String,
                             examples: List[String],
                             ipa: String,
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             language: Language,
                             source: DefinitionSource,
                             token: String)
    extends Definition {
  override lazy val toDTO: DefinitionDTO =
    GenericDefinitionDTO(subdefinitions, tag, examples)
}
