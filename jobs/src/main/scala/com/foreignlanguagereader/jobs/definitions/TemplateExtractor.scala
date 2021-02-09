package com.foreignlanguagereader.jobs.definitions

object TemplateExtractor {
  val leftBrace = "\\{"
  val rightBrace = "\\}"
  val pipe = "\\|"
  val notPipeCaptureGroup = "([^|\\{]+)"
  val notRightBraceCaptureGroup = "([^\\}]*)"

  val templateRegex: String =
    leftBrace + leftBrace + notPipeCaptureGroup + pipe + notRightBraceCaptureGroup + rightBrace + rightBrace
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
  val extractTemplatesFromString: String => Array[Array[String]] =
    (input: String) =>
      templateRegex.r
        .findAllIn(input)
        .matchData
        .map(m => Array(m.group(1), m.group(2)))
        .toArray
case class WiktionaryGenericText(text: String)
