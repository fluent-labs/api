package com.foreignlanguagereader.jobs.definitions

import com.databricks.spark.xml._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, element_at, posexplode, udf}
import org.apache.spark.sql.{Dataset, SparkSession}

object TemplateExtractor {
  val leftBrace = "\\{"
  val rightBrace = "\\}"
  val pipe = "\\|"
  val notPipeCaptureGroup = "([^|\\{]+)"
  val notRightBraceCaptureGroup = "([^\\}]*)"

  val templateRegex: String =
    leftBrace + leftBrace + notPipeCaptureGroup + pipe + notRightBraceCaptureGroup + rightBrace + rightBrace
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
  )(implicit spark: SparkSession): Dataset[WiktionaryGenericText] = {
    import spark.implicits._

    spark.read
      .option("rowTag", "page")
      .xml(path)
      .select("revision.text._VALUE")
      .withColumnRenamed("_VALUE", "text")
      .as[WiktionaryGenericText]
  }

  def extractTemplateInstances(
      data: Dataset[WiktionaryGenericText]
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
      templateRegex.r
        .findAllIn(input)
        .matchData
        .map(m => Array(m.group(1), m.group(2)))
        .toArray

  val regexp_extract_templates: UserDefinedFunction = udf(
    extractTemplatesFromString
  )
}

case class WiktionaryGenericText(text: String)
case class WiktionaryTemplateInstance(name: String, arguments: String)
case class WiktionaryTemplate(name: String, count: BigInt)
