package com.foreignlanguagereader.api.domain.definition.entry

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition,
  HSKLevel
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.sksamuel.elastic4s.{Hit, HitReader}
import play.api.libs.json._

import scala.util.{Success, Try}

object DefinitionSource extends Enumeration {
  type DefinitionSource = Value
  val WIKTIONARY, CEDICT, MULTIPLE = Value

  // Makes sure we can serialize and deserialize this to JSON
  implicit val sourceFormat: Format[DefinitionSource] =
    new Format[DefinitionSource] {
      def reads(json: JsValue) =
        JsSuccess(DefinitionSource.withName(json.as[String]))
      def writes(source: DefinitionSource.DefinitionSource) =
        JsString(source.toString)
    }
}

// Companion object is at bottom of file
sealed trait DefinitionEntry {
  val subdefinitions: List[String]
  val language: Language
  val source: DefinitionSource
  val token: String
}

case class CEDICTDefinitionEntry(override val subdefinitions: List[String],
                                 pinyin: String,
                                 simplified: String,
                                 traditional: String,
                                 override val token: String)
    extends DefinitionEntry {
  val language: Language = Language.CHINESE
  val source: DefinitionSource = DefinitionSource.CEDICT
}

object CEDICTDefinitionEntry {
  // Allows serializing and deserializing in json
  implicit val reads: Reads[CEDICTDefinitionEntry] =
    Json.reads[CEDICTDefinitionEntry]
  implicit val readsSeq: Reads[Seq[CEDICTDefinitionEntry]] =
    Reads.seq(reads)

  implicit def convertToDefinition(
    c: CEDICTDefinitionEntry
  ): ChineseDefinition =
    ChineseDefinition(
      c.subdefinitions,
      tag = "",
      examples = List(),
      c.pinyin,
      c.simplified,
      c.traditional,
      HSKLevel.NONE,
      DefinitionSource.CEDICT,
      c.token
    )

  // Lets us pull this from elasticsearch
  implicit object CEDICTHitReader extends HitReader[CEDICTDefinitionEntry] {
    override def read(hit: Hit): Try[CEDICTDefinitionEntry] = {
      val source = hit.sourceAsMap
      Success(
        CEDICTDefinitionEntry(
          source("subdefinitions").asInstanceOf[List[String]],
          source("pinyin").toString,
          source("simplified").toString,
          source("traditional").toString,
          source("token").toString
        )
      )
    }
  }
}

case class WiktionaryDefinitionEntry(override val subdefinitions: List[String],
                                     tag: String,
                                     examples: List[String],
                                     override val language: Language,
                                     override val token: String)
    extends DefinitionEntry {
  val source: DefinitionSource = DefinitionSource.CEDICT
}

object WiktionaryDefinitionEntry {
  // Allows serializing and deserializing in json
  implicit val reads: Reads[WiktionaryDefinitionEntry] =
    Json.reads[WiktionaryDefinitionEntry]
  implicit val readsSeq: Reads[Seq[WiktionaryDefinitionEntry]] =
    Reads.seq(reads)

  implicit def convertToDefinition(w: WiktionaryDefinitionEntry): Definition =
    Definition(
      w.subdefinitions,
      w.tag,
      w.examples,
      w.language,
      DefinitionSource.WIKTIONARY,
      w.token
    )

  // Used for elasticsearch
  implicit object WiktionaryHitReader
      extends HitReader[WiktionaryDefinitionEntry] {
    override def read(hit: Hit): Try[WiktionaryDefinitionEntry] = {
      val source = hit.sourceAsMap
      Success(
        WiktionaryDefinitionEntry(
          source("subdefinitions").asInstanceOf[List[String]],
          source("tag").toString,
          source("examples").toString.asInstanceOf[List[String]],
          Language.withName(source("language").toString),
          source("token").toString
        )
      )
    }
  }
}

object DefinitionEntry {
  // Deserializing from JSON
  implicit val reads: Reads[DefinitionEntry] =
    Json.reads[DefinitionEntry]
  implicit val readsSeq: Reads[Seq[DefinitionEntry]] =
    Reads.seq(reads)

  // Mapping DefinitionEntry => Definition
  // Delegates to their own implicit converters
  implicit def convertToDefinition(d: DefinitionEntry): Definition =
    d match {
      case c: CEDICTDefinitionEntry     => c
      case w: WiktionaryDefinitionEntry => w
    }
  implicit def convertToSeqOfDefinitions(
    d: Seq[DefinitionEntry]
  ): Seq[Definition] = d.map(e => convertToDefinition(e))

  // For elasticsearch - this does dynamic dispatch based on the type
  implicit object DefinitionEntryHitReader extends HitReader[DefinitionEntry] {
    override def read(hit: Hit): Try[DefinitionEntry] = {
      val source = hit.sourceAsMap
      source("source") match {
        case DefinitionSource.CEDICT =>
          CEDICTDefinitionEntry.CEDICTHitReader.read(hit)
        case DefinitionSource.WIKTIONARY =>
          WiktionaryDefinitionEntry.WiktionaryHitReader.read(hit)
      }
    }
  }
}
