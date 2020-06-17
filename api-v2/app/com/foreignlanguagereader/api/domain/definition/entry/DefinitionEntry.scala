package com.foreignlanguagereader.api.domain.definition.entry

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.sksamuel.elastic4s.{Hit, HitReader}
import play.api.libs.json._

import scala.util.Try

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

  // For elasticsearch - this does dynamic dispatch based on the type
  implicit object DefinitionEntryHitReader extends HitReader[DefinitionEntry] {
    override def read(hit: Hit): Try[DefinitionEntry] = {
      val source = hit.sourceAsMap
      DefinitionSource.withName(source("source").toString) match {
        case DefinitionSource.CEDICT =>
          CEDICTDefinitionEntry.CEDICTHitReader.read(hit)
        case DefinitionSource.WIKTIONARY =>
          WiktionaryDefinitionEntry.WiktionaryHitReader.read(hit)
        case _ => throw new IllegalStateException("This should be impossible.")
      }
    }
  }
}
