package com.foreignlanguagereader.api.domain.definition.entry

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.sksamuel.elastic4s.{Hit, HitReader}
import play.api.libs.json.{Format, Json, Reads}

import scala.util.{Success, Try}

case class WiktionaryDefinitionEntry(override val subdefinitions: List[String],
                                     tag: String,
                                     examples: List[String],
                                     override val wordLanguage: Language,
                                     override val definitionLanguage: Language,
                                     override val token: String,
                                     override val source: DefinitionSource =
                                       DefinitionSource.WIKTIONARY)
    extends DefinitionEntry {
  override lazy val toDefinition: Definition = wordLanguage match {
    case Language.CHINESE =>
      ChineseDefinition(
        subdefinitions,
        tag,
        examples,
        definitionLanguage = definitionLanguage,
        source = source,
        token = token
      )
    case _ =>
      Definition(
        subdefinitions,
        tag,
        examples,
        wordLanguage,
        definitionLanguage,
        DefinitionSource.WIKTIONARY,
        token
      )
  }
}

object WiktionaryDefinitionEntry {
  // Allows serializing and deserializing in json
  implicit val format: Format[WiktionaryDefinitionEntry] =
    Json.format[WiktionaryDefinitionEntry]
  implicit val readsSeq: Reads[Seq[WiktionaryDefinitionEntry]] =
    Reads.seq(format.reads)

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
          Language.withName(source("wordLanguage").toString),
          Language.withName(source("definitionLanguage").toString),
          source("token").toString
        )
      )
    }
  }
}
