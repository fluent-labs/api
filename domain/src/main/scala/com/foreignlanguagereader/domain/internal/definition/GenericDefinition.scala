package com.foreignlanguagereader.domain.internal.definition

import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.dto.v1.definition.{
  DefinitionDTO,
  GenericDefinitionDTO
}
import play.api.libs.json.{Format, Json}

case class GenericDefinition(
    subdefinitions: List[String],
    ipa: String,
    tag: PartOfSpeech,
    examples: Option[List[String]],
    // These fields are needed for elasticsearch lookup
    // But do not need to be presented to the user.
    definitionLanguage: Language,
    wordLanguage: Language,
    source: DefinitionSource,
    token: String
) extends Definition {
  val id: String = generateId()

  override lazy val toDTO: DefinitionDTO =
    GenericDefinitionDTO(
      id = id,
      subdefinitions = subdefinitions,
      tag = PartOfSpeech.toDTO(tag),
      examples = examples
    )
}
object GenericDefinition {
  implicit val format: Format[GenericDefinition] =
    Json.format[GenericDefinition]
}
