package com.foreignlanguagereader.jobs.definitions.exploration

import com.databricks.spark.xml._
import com.foreignlanguagereader.jobs.definitions.{
  WiktionaryRawText,
  WiktionaryTemplate,
  WiktionaryTemplateInstance
}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, element_at, posexplode, udf}
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.{Failure, Success, Try}

object TemplateExtractor {
  val leftBrace = "\\{"
  val rightBrace = "\\}"
  val pipe = "\\|"
  val notPipeCaptureGroup: String = "([^" + pipe + rightBrace + "]+)"
  val notRightBraceCaptureGroup: String =
    "(" + pipe + "[^" + rightBrace + "]*)?"

  val templateRegex: String =
    leftBrace + leftBrace + notPipeCaptureGroup + notRightBraceCaptureGroup + rightBrace + rightBrace

  println(s"Template regex: $templateRegex")

  val backupsBasePath =
    "s3a://foreign-language-reader-content/definitions/wiktionary/"

  val backups = Map(
    "simple" -> "simplewiktionary-20200301-pages-meta-current.xml"
  )

  def main(args: Array[String]): Unit = {
    implicit val spark: SparkSession = SparkSession.builder
      .appName(s"Wiktionary Template Extractor")
      .getOrCreate()

    backups.foreach { case (dictionary, path) =>
      extractTemplatesFromBackup(
        s"$backupsBasePath/$path",
        s"$backupsBasePath/templates/$dictionary"
      )
    }
  }

  def extractTemplatesFromBackup(path: String, resultPath: String)(implicit
      spark: SparkSession
  ): Unit = {
    val templateInstances =
      extractTemplateInstances(loadWiktionaryDump(path)).cache()
    templateInstances.write.csv(s"$resultPath/instances.csv")

    val templates = extractTemplateCount(templateInstances)
    templates.write.csv(s"$resultPath/templates.csv")
  }

  def loadWiktionaryDump(
      path: String
  )(implicit spark: SparkSession): Dataset[WiktionaryRawText] = {
    import spark.implicits._

    spark.read
      .option("rowTag", "page")
      .xml(path)
      .select("revision.text._VALUE")
      .withColumnRenamed("_VALUE", "text")
      .as[WiktionaryRawText]
  }

  def extractTemplateInstances(
      data: Dataset[WiktionaryRawText]
  )(implicit spark: SparkSession): Dataset[WiktionaryTemplateInstance] = {
    import spark.implicits._

    data
      .select(posexplode(regexp_extract_templates(col("text"))))
      .select(
        element_at(col("col"), 1),
        element_at(col("col"), 2)
      ) // Columns start at 1 not 0
      .withColumnRenamed("element_at(col, 1)", "name")
      .withColumnRenamed("element_at(col, 2)", "arguments")
      .sort("name")
      .as[WiktionaryTemplateInstance]
  }

  def extractTemplateCount(
      data: Dataset[WiktionaryTemplateInstance]
  )(implicit spark: SparkSession): Dataset[WiktionaryTemplate] = {
    import spark.implicits._

    data.groupBy("name").count().sort(col("count").desc).as[WiktionaryTemplate]
  }

  val extractTemplatesFromString: String => Array[Array[String]] =
    (input: String) =>
      Try(
        templateRegex.r
          .findAllIn(input)
          .matchData
          .map(m => {
            val templateName = m.group(1)
            val arguments = if (m.groupCount == 2) m.group(2) else ""
            Array(templateName, arguments)
          })
          .toArray
      ) match {
        case Success(value) => value
        case Failure(_)     => Array(Array("Error", input))
      }

  val regexp_extract_templates: UserDefinedFunction = udf(
    extractTemplatesFromString
  )
}
