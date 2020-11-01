import com.databricks.spark.xml._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

case class WiktionaryRawEntry(id: Long, token: String, text: String)

object Wiktionary {
  val metaArticleTitles: Set[String] =
    Set(
      "MediaWiki:",
      "MediaWiki talk:",
      "Template:",
      "Template talk:",
      "Wiktionary:",
      "Wiktionary talk:",
      "User:",
      "User talk:",
      "Category:",
      "Category talk:",
      "Help:",
      "File:",
      "File talk:",
      "Appendix:",
      "Module:",
      "Module talk:",
      "Talk:"
    )

  def loadWiktionaryDump(
    path: String
  )(implicit spark: SparkSession): Dataset[WiktionaryRawEntry] = {
    import spark.implicits._

    spark.read
      .option("rowTag", "page")
      .xml(path)
      .select("revision.text._VALUE", "title", "id")
      .withColumnRenamed("title", "token")
      .withColumnRenamed("_VALUE", "text")
      .filter(row => filterMetaArticles(row))
      .as[WiktionaryRawEntry]
  }

  def filterMetaArticles(row: Row): Boolean = {
    val title = row.getAs[String]("token")
    metaArticleTitles.forall(prefix => !title.startsWith(prefix))
  }

  val caseInsensitiveFlag = "(?i)"
  val periodMatchesNewlineFlag = "(?s)"
  val oneOrMoreEqualsSign = "=+"
  val doubleEqualsSign = "=="
  val tripleEqualsSign = "==="
  val optionalWhiteSpace = " *"
  val anythingButEqualsSign = "[^=]*"
  val lazyMatchAnything = "(.*?)"
  val spaceOrNewline = "[ |\n]+"
  val nextSection = s"(?>== *[A-Za-z0-9]+ *==$spaceOrNewline)"
  val nextSectionOrEndOfFile = s"(?>$nextSection|\\Z)+"

  def headingRegex(equalsCount: Int): String =
    "=".repeat(equalsCount) + optionalWhiteSpace + anythingButEqualsSign + optionalWhiteSpace + "="
      .repeat(equalsCount) + anythingButEqualsSign // Needed or else outer equals will be ignored
  // Subtle but '== Test ==' will match '=== Test ===' at this point: '="== Test =="='

  def sectionRegex(sectionName: String): String =
    periodMatchesNewlineFlag + caseInsensitiveFlag + doubleEqualsSign + optionalWhiteSpace + sectionName + optionalWhiteSpace + doubleEqualsSign + lazyMatchAnything + nextSectionOrEndOfFile

  def subSectionRegex(sectionName: String): String =
    periodMatchesNewlineFlag + caseInsensitiveFlag + tripleEqualsSign + optionalWhiteSpace + sectionName + optionalWhiteSpace + tripleEqualsSign + lazyMatchAnything + nextSectionOrEndOfFile

  def getHeadings(data: Dataset[WiktionaryRawEntry],
                  equalsCount: Integer): DataFrame = {
    data
      .select(explode(findHeadings(equalsCount)(col("text"))))
      .distinct()
      .coalesce(1)
      .sort(col("col"))
  }
  def findHeadings: Int => UserDefinedFunction =
    (equalsCount: Int) =>
      udf(
        (text: String) =>
          text.linesIterator
            .filter(line => line.matches(headingRegex(equalsCount)))
            .map(
              line =>
                line.replaceAll("=".repeat(equalsCount), "").trim().toLowerCase
            )
            .toArray
    )

  def extractSection(data: DataFrame, name: String): DataFrame =
    data.withColumn(
      name.toLowerCase(),
      regexp_extract(col("text"), sectionRegex(name), 1)
    )

  def extractSections(data: Dataset[WiktionaryRawEntry],
                      sections: Array[String]): DataFrame = {
    sections
      .foldLeft(data.toDF())((data, section) => extractSection(data, section))
  }

  def extractSubsection(data: DataFrame, name: String): DataFrame =
    data.withColumn(
      name.toLowerCase(),
      regexp_extract(col("text"), subSectionRegex(name), 1)
    )
  def extractSubsections(data: DataFrame,
                         sections: Array[String]): DataFrame = {
    sections.foldLeft(data)((data, section) => extractSection(data, section))
  }

  // Defined in SPARK-24884 but not released yet
  val regexp_extract_all: (String, Int) => UserDefinedFunction =
    (regex: String, captureGroupIndex: Int) =>
      udf((input: String) => {
        regex.r
          .findAllIn(input)
          .matchData
          .map(m => m.group(captureGroupIndex))
          .toArray
      })
}
