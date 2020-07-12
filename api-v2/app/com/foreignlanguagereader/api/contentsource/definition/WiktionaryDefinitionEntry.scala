package com.foreignlanguagereader.api.contentsource.definition

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.{
  ChineseDefinition,
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.sksamuel.elastic4s.{Hit, HitReader}
import play.api.libs.json.{Format, Json, Reads}

import scala.util.{Failure, Success, Try}

case class WiktionaryDefinitionEntry(override val subdefinitions: List[String],
                                     tag: Option[PartOfSpeech],
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

      val wordLanguage =
        Language.fromString(source("wordLanguage").toString)
      val definitionLanguage =
        Language.fromString(source("definitionLanguage").toString)

      (wordLanguage, definitionLanguage) match {
        case (Some(word), Some(definition)) =>
          Success(
            WiktionaryDefinitionEntry(
              source("subdefinitions").asInstanceOf[List[String]],
              // TODO HUGE TODO HERE
              // Leaving it for now since we're gonna set up our own wiktionary parsing.
//            source("tag").toString,
              None,
              source("examples").toString.asInstanceOf[List[String]],
              word,
              definition,
              source("token").toString
            )
          )
        case _ =>
          val invalidInput: String =
            if (definitionLanguage.isDefined) source("wordLanguage").toString
            else source("definitionLanguage").toString
          Failure(
            new IllegalArgumentException(
              s"Invalid language $invalidInput returned from elasticsearch"
            )
          )
      }
    }
  }
}