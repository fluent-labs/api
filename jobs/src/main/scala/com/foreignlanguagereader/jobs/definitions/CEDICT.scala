package com.foreignlanguagereader.jobs.definitions

import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.matching.Regex

object CEDICT
    extends DefinitionsParsingJob[CEDICTDefinitionEntry](
      "s3a://foreign-language-reader-content/definitions/cedict/",
      "cedict_1_0_ts_utf-8_mdbg.zip",
      DefinitionSource.CEDICT
    ) {
  val lineRegex: Regex = "/([^ ]+)\\s([^ ]+) \\[(.*)\\] \\/(.*)\\/\\n/g".r

  override def loadFromPath(path: String)(implicit
      spark: SparkSession
  ): Dataset[CEDICTDefinitionEntry] = {
    import spark.implicits._
    spark.read
      .textFile(path)
      // These lines are license and parsing instructions
      .filter(line => !line.startsWith("#"))
      // Error guard
      .filter(line => line.matches(lineRegex.regex))
      .map(line => {
        val lineRegex(traditional, simplified, pinyin, definitions) = line
        val subdefinitions = definitions.split("/")

        CEDICTDefinitionEntry(
          subdefinitions = subdefinitions.toList,
          pinyin = pinyin,
          simplified = simplified,
          traditional = traditional,
          token = traditional
        )
      })
  }
}
