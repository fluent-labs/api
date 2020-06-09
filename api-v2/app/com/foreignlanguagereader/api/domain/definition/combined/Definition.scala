package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.GenericDefinitionDTO

class Definition(val subdefinitions: List[String],
                 val tag: String,
                 val examples: List[String],
                 // These fields are needed for elasticsearch lookup
                 // But do not need to be presented to the user.
                 val language: Language,
                 val source: DefinitionSource,
                 val token: String)
object Definition {
  def apply(subdefinitions: List[String],
            tag: String,
            examples: List[String],
            language: Language,
            source: DefinitionSource,
            token: String): Definition =
    new Definition(subdefinitions, tag, examples, language, source, token)
  def unapply(d: Definition): Option[
    (List[String], String, List[String], Language, DefinitionSource, String)
  ] = Some(d.subdefinitions, d.tag, d.examples, d.language, d.source, d.token)

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
}
