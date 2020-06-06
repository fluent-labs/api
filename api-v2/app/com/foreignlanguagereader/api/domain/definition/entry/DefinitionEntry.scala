package com.foreignlanguagereader.api.domain.definition.entry

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}

abstract class DefinitionEntry(val subdefinitions: List[String],
                               // These fields are needed for elasticsearch lookup
                               // But do not need to be presented to the user.
                               val language: Language,
                               val source: DefinitionSource,
                               val token: String) {}

object DefinitionEntry {
  // This exists to bring each type of definition entry into scope
  // That way the client can handle each different type of entry without knowing how to handle it
  // And still return a uniform model to the service
  implicit def convertToDefinition(
    definitionEntry: DefinitionEntry
  ): Definition = definitionEntry match {
    case w: WiktionaryDefinitionEntry => w
    case c: CEDICTDefinitionEntry     => c
  }
  implicit def convertToSeqOfDefinitions(
    definitions: Seq[DefinitionEntry]
  ): Seq[Definition] = definitions.map(x => convertToDefinition(x))
}

object DefinitionSource extends Enumeration {
  type DefinitionSource = Value
  val WIKTIONARY, CEDICT, MULTIPLE = Value

  implicit val sourceFormat: Format[DefinitionSource] =
    new Format[DefinitionSource] {
      def reads(json: JsValue) =
        JsSuccess(DefinitionSource.withName(json.as[String]))
      def writes(source: DefinitionSource.DefinitionSource) =
        JsString(source.toString)
    }
}
