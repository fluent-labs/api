import com.databricks.spark.xml._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

class Wiktionary(implicit spark: SparkSession) {}
object Wiktionary {
  val ignoredTitles: Set[String] =
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

  val simpleWiktionaryBucket = "/"

  def loadWiktionaryDump(
    path: String
  )(implicit spark: SparkSession): DataFrame = {
    spark.read
      .option("rowTag", "page")
      .xml(path)
      .filter(row => filterMetaArticles(row))
      .select("revision.text._VALUE", "token")
      .withColumnRenamed("_VALUE", "text")
  }

  def filterMetaArticles(row: Row): Boolean = {
    val title = row.getAs[String]("token")
    ignoredTitles.forall(prefix => !title.startsWith(prefix))
  }

  val caseInsensitiveFlag = "(?i)"
  val periodMatchesNewlineFlag = "(?s)"
  val oneOrMoreEqualsSign = "=+"
  val doubleEqualsSign = "=="
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

  def getHeadings(data: DataFrame, equalsCount: Integer): DataFrame = {
    data
      .select(explode(findHeadings(equalsCount)(col("text"))))
      .distinct()
      .coalesce(1)
  }
  def findHeadings: Int => UserDefinedFunction =
    (equalsCount: Int) =>
      udf(
        (text: String) =>
          text.linesIterator
            .filter(line => line.matches(headingRegex(equalsCount)))
            .toArray
    )

  def extractSection(data: DataFrame, name: String): DataFrame =
    data.withColumn(
      name.toLowerCase(),
      regexp_extract(col("text"), sectionRegex(name), 1)
    )

  def extractSections(data: DataFrame, sections: Array[String]): DataFrame = {
    sections.foldLeft(data)((data, section) => extractSection(data, section))
  }
}
