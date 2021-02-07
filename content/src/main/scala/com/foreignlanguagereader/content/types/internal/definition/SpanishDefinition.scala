package com.foreignlanguagereader.content.types.internal.definition

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO

import scala.collection.JavaConverters._

case class SpanishDefinition(
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
    new DefinitionDTO(
      id,
      subdefinitions.asJava,
      PartOfSpeech.toDTO(tag),
      DefinitionSource.toDTO(source),
      examples.getOrElse(List()).asJava
    )
}
