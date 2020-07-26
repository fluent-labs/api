package com.foreignlanguagereader.api.domain.definition

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.api.dto.v1.definition.{
  DefinitionDTO,
  GenericDefinitionDTO
}
import play.api.libs.json.{Format, Json}

case class GenericDefinition(subdefinitions: List[String],
                             ipa: String,
                             tag: Option[PartOfSpeech],
                             examples: Option[List[String]],
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             definitionLanguage: Language,
                             wordLanguage: Language,
                             source: DefinitionSource,
                             token: String)
    extends Definition {
  val id: String = generateId()

  override lazy val toDTO: DefinitionDTO =
    GenericDefinitionDTO(
      id = id,
      subdefinitions = subdefinitions,
      tag = tag,
      examples = examples
    )
}
object GenericDefinition {
  implicit val format: Format[GenericDefinition] =
    Json.format[GenericDefinition]
}
