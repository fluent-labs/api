package com.foreignlanguagereader.api.repository.definition

import cats.implicits._
import com.foreignlanguagereader.domain.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.domain.internal.word.Word
import play.api.Logger

import scala.io.{BufferedSource, Source}
import scala.util.matching.Regex
import scala.util.{Failure, Success, Using}

object Cedict {
  val logger: Logger = Logger(this.getClass)

  private[this] val CEDICTPath = "/resources/definition/chinese/cedict_ts.u8"

  val definitions: Map[String, List[CEDICTDefinitionEntry]] =
    Using(
      Source.fromInputStream(
        this.getClass
          .getResourceAsStream(CEDICTPath)
      )
    ) { cedict =>
      parseCedictFile(cedict).groupBy(_.traditional)
    } match {
      case Success(d) =>
        val entries = d.values.map(_.size).sum
        val duplicates = d.values.count(_.size > 1)
        logger.info(
          s"Successfully loaded CEDICT with $entries entries and $duplicates words with multiple entries"
        )
        d
      case Failure(e) =>
        throw new IllegalStateException("Failed to load CEDICT", e)
    }

  def getDefinition(word: Word): Option[List[CEDICTDefinitionEntry]] =
    // If it's already traditional then we can just do the lookup
    if (definitions.keySet.contains(word.processedToken))
      definitions.get(word.processedToken)
    else {
      // If not we need to get all traditional characters for the simplified one, and give all the definitions.
      val d = simplifiedToTraditionalMapping
        .getOrElse(word.processedToken, List())
        .flatMap(trad => definitions.get(trad))
        .flatten
      if (d.isEmpty) None else Some(d)
    }

  private[this] val simplifiedToTraditionalMapping: Map[String, List[String]] =
    definitions.map {
      case (traditional, entries) => traditional -> entries.map(_.simplified)
    }

  private[this] def parseCedictFile(
      cedict: BufferedSource
  ): List[CEDICTDefinitionEntry] = {
    val cedictLineRegex: Regex = """(\S+) (\S+) \[(.+)\] \/(.+)\/""".r
    cedict
      .getLines()
      // These lines are all dictionary comments. License, schema, etc.
      .filterNot(_.startsWith("#"))
      .flatMap {
        case cedictLineRegex(traditional, simplified, pinyin, definition) =>
          val subdefinitions = definition.split("/").toList
          CEDICTDefinitionEntry(
            subdefinitions,
            pinyin,
            simplified,
            traditional,
            traditional
          ).some
        case _ => None
      }
      .toList
  }
}
