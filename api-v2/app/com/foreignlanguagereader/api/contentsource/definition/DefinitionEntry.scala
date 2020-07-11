package com.foreignlanguagereader.api.contentsource.definition

import com.foreignlanguagereader.api.contentsource.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource
}
import play.api.libs.json._

trait DefinitionEntry {
  val subdefinitions: List[String]
  val wordLanguage: Language
  val definitionLanguage: Language
  val source: DefinitionSource
  val token: String

  val toDefinition: Definition
}

object DefinitionEntry {
  // Smart json handling that dispatches to the correct class
  implicit val formatDefinitionEntry: Format[DefinitionEntry] =
    new Format[DefinitionEntry] {
      override def reads(json: JsValue): JsResult[DefinitionEntry] = {
        json \ "source" match {
          case JsDefined(JsString(source)) =>
            DefinitionSource.fromString(source) match {
              case Some(DefinitionSource.CEDICT) =>
                CEDICTDefinitionEntry.format.reads(json)
              case Some(DefinitionSource.WIKTIONARY) =>
                WiktionaryDefinitionEntry.format.reads(json)
              case _ =>
                JsError("Unknown definition source")
            }
          case _ =>
            JsError(
              "Definition source was not defined, cannot decide how to handle"
            )
        }
      }
      override def writes(o: DefinitionEntry): JsValue = o match {
        case c: CEDICTDefinitionEntry => CEDICTDefinitionEntry.format.writes(c)
        case w: WiktionaryDefinitionEntry =>
          WiktionaryDefinitionEntry.format.writes(w)
      }
    }
  implicit val readsSeq: Reads[Seq[DefinitionEntry]] =
    Reads.seq(formatDefinitionEntry)

  def convertToSeqOfDefinitions(d: Seq[DefinitionEntry]): Seq[Definition] =
    d.map(e => e.toDefinition)
}
