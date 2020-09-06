import com.databricks.spark.xml._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

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
      .select("revision.text._VALUE", "title")
      .withColumnRenamed("_VALUE", "text")
  }

  def loadSimple(
    path: String
  )(implicit spark: SparkSession): Dataset[SimpleWiktionary] = {
    import spark.implicits._
    extractSections(loadWiktionaryDump(path), SimpleWiktionary.sectionNames)
      .drop("text")
      .as[SimpleWiktionary]
  }

  def filterMetaArticles(row: Row): Boolean = {
    val title = row.getAs[String]("title")
    ignoredTitles.forall(prefix => !title.startsWith(prefix))
  }

  val oneOrMoreEqualsSign = "=+"
  val optionalWhiteSpace = " *"
  val anythingButEqualsSign = "[^=]*"
  val sectionNameRegex
    : String = oneOrMoreEqualsSign + optionalWhiteSpace + anythingButEqualsSign + optionalWhiteSpace + oneOrMoreEqualsSign + ".*"
  def sectionRegex(sectionName: String): String =
    oneOrMoreEqualsSign + optionalWhiteSpace + sectionName + optionalWhiteSpace + oneOrMoreEqualsSign + s"($anythingButEqualsSign)" // Capture group

  def getSections(data: DataFrame): DataFrame = {
    data.select(explode(findSections(col("text")))).distinct().coalesce(1)
  }
  val findSections: UserDefinedFunction = udf(
    (text: String) =>
      text.linesIterator
        .filter(line => line.matches(sectionNameRegex))
        .map(_.replaceAll("=+", ""))
        .toArray
  )

  def extractSection(data: DataFrame, name: String): DataFrame =
    data.withColumn(
      name.toLowerCase(),
      regexp_extract(col("text"), sectionRegex(name), 0)
    )

  def extractSections(data: DataFrame, sections: List[String]): DataFrame = {
    sections.foldLeft(data)((data, section) => extractSection(data, section))
  }
}
