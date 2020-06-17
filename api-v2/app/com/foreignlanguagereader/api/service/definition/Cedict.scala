package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.domain.definition.entry.CEDICTDefinitionEntry
import play.api.Logger

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Using}

object Cedict {
  val logger: Logger = Logger(this.getClass)

  private[this] val CEDICTPath = "/resources/definition/chinese/cedict_ts.u8"

  val definitions: Map[String, CEDICTDefinitionEntry] =
    Using(
      Source.fromInputStream(
        this.getClass
          .getResourceAsStream(CEDICTPath)
      )
    ) { cedict =>
      parseCedictFile(cedict).map(entry => entry.traditional -> entry).toMap
    } match {
      case Success(d) =>
        logger.info("Successfully loaded CEDICT")
        d
      case Failure(e) =>
        throw new IllegalStateException("Failed to load CEDICT", e)
    }

  def getDefinition(token: String): Option[CEDICTDefinitionEntry] =
    definitions.get(token)

  private[this] val simplifiedToTraditionalMapping: Map[String, String] =
    definitions.values.map(entry => entry.simplified -> entry.traditional).toMap
  private[this] val simplified: Set[String] =
    definitions.values.map(_.simplified).toSet
  private[this] val traditional: Set[String] =
    definitions.values.map(_.traditional).toSet

  /**
    * Converts a given token into traditional Chinese
    * @param token The token to normalize
    * @return The token as traditional
    */
  def convertToTraditional(token: String): String = token match {
    case t if traditional.contains(t) =>
      logger.info(
        s"Token $token was already traditional, no normalization needed."
      )
      t
    case s if simplified.contains(s) =>
      val t = simplifiedToTraditionalMapping.getOrElse(s, token)
      logger.info(
        s"Token $token normalized from simplified $s to traditional $t."
      )
      t
    case _ =>
      logger.warn(s"Unknown token $token, unable to normalize.")
      token
  }

  private[this] def parseCedictFile(
    cedict: BufferedSource
  ): List[CEDICTDefinitionEntry] =
    cedict
      .getLines()
      // These lines are all dictionary comments. License, schema, etc.
      .filterNot(_.startsWith("#"))
      .map(line => {
        val s"$traditional $simplified [$pinyin] /$definition/" = line
        val subdefinitions = definition.split("/").toList
        CEDICTDefinitionEntry(
          subdefinitions,
          pinyin,
          simplified,
          traditional,
          traditional
        )
      })
      .toList
}
