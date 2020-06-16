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

case class CEDICTDefinitionEntry(override val subdefinitions: List[String],
                                 pinyin: String,
                                 simplified: String,
                                 traditional: String,
                                 override val token: String)
    extends DefinitionEntry {
  override val wordLanguage: Language = Language.CHINESE
  override val definitionLanguage: Language = Language.ENGLISH
  override val source: DefinitionSource = DefinitionSource.CEDICT

  override lazy val toDefinition: Definition = ChineseDefinition(
    subdefinitions,
    tag = "",
    examples = List(),
    pinyin,
    simplified,
    traditional,
    definitionLanguage,
    DefinitionSource.CEDICT,
    token
  )
}

object CEDICTDefinitionEntry {
  // Allows serializing and deserializing in json
  implicit val format: Format[CEDICTDefinitionEntry] =
    Json.format[CEDICTDefinitionEntry]
  implicit val readsSeq: Reads[Seq[CEDICTDefinitionEntry]] =
    Reads.seq(format.reads)

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
