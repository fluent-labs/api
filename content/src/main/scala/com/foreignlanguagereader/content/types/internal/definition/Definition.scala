package com.foreignlanguagereader.content.types.internal.definition

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO

trait Definition {
  val subdefinitions: List[String]
  val ipa: String
  val tag: PartOfSpeech
  val examples: Option[List[String]]
  // These fields are needed for elasticsearch lookup
  // But do not need to be presented to the user.
  val definitionLanguage: Language
  val wordLanguage: Language
  val source: DefinitionSource
  val token: String

  // We need a way to uniquely identify parts of speech
  // Some level of collisions are unavoidable but they should be as rare as possible.
  val id: String
  def generateId(): String = s"$wordLanguage:$token:$ipa:$tag"

  // This always needs to know how to convert itself to a DTO
  val toDTO: DefinitionDTO
}
