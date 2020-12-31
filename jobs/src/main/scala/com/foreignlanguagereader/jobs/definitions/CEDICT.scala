package com.foreignlanguagereader.jobs.definitions

import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

object CEDICT
    extends DefinitionsParsingJob[CEDICTDefinitionEntry](
      "s3a://foreign-language-reader-content/definitions/cedict/",
      "cedict_ts.u8",
      DefinitionSource.CEDICT
    ) {
  val lineRegex: Regex = "([^ ]+)\\s([^ ]+) \\[(.*)\\] \\/(.*)\\/".r

  override def loadFromPath(path: String)(implicit
      spark: SparkSession
  ): Dataset[CEDICTDefinitionEntry] = {
    import spark.implicits._
    spark.read
      .textFile(path)
      // These lines are license and parsing instructions
      .filter(line => !line.startsWith("#"))
      .map(line => {
        Try({
          val lineRegex(traditional, simplified, pinyin, definitions) = line
          val subdefinitions = definitions.split("/")

          CEDICTDefinitionEntry(
            subdefinitions = subdefinitions.toList,
            pinyin = pinyin,
            simplified = simplified,
            traditional = traditional,
            token = traditional
          )
        }) match {
          case Success(value) => value
          case Failure(exception) =>
            log.error(
              s"Error parsing line $line: ${exception.getMessage}",
              exception
            )
            CEDICTDefinitionEntry(List(), "ERROR", "ERROR", "ERROR", line)
        }
      })
  }
}
