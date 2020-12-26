package com.foreignlanguagereader.content.types.internal.definition

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.util.JsonSequenceHelper
import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO
import play.api.libs.json._

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

object Definition {
  def apply(
      subdefinitions: List[String],
      ipa: String,
      tag: PartOfSpeech,
      examples: Option[List[String]],
      wordLanguage: Language,
      definitionLanguage: Language,
      source: DefinitionSource,
      token: String
  ): Definition =
    definition.GenericDefinition(
      subdefinitions,
      ipa,
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

  // Json
  implicit val format: Format[Definition] =
    new Format[Definition] {
      override def reads(json: JsValue): JsResult[Definition] = {
        (json \ "wordLanguage").validate[Language] match {
          case Language.CHINESE => ChineseDefinition.reads.reads(json)
          case _                => GenericDefinition.format.reads(json)
        }
      }
      override def writes(o: Definition): JsValue =
        o match {
          case c: ChineseDefinition => ChineseDefinition.writes.writes(c)
          case g: GenericDefinition => GenericDefinition.format.writes(g)
        }
    }
  implicit val helper: JsonSequenceHelper[Definition] =
    new JsonSequenceHelper[Definition]
}
