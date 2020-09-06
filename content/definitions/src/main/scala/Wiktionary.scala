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

  val sectionNames = List(
    "Examples",
    "Noun",
    "Expression",
    "Homophones",
    "Prefix",
    "Synonyms",
    "Symbol",
    "Initialism",
    "Abbreviation",
    "Suffix",
    "Contraction",
    "Notes",
    "Conjunction",
    "Antonym",
    "Phrases",
    "Antonyms",
    "Gallery",
    "References",
    "Adverb",
    "Determiner",
    "Usage",
    "Synonym",
    "Acronym",
    "Interjection",
    "Determinative",
    "Preposition",
    "Adjective",
    "Etymology",
    "Pronoun",
    "Phrase",
    "Verb",
    "Numerals",
    "Pronunciation"
  )

  val simpleWiktionaryBucket = "/"

  def loadWiktionaryDump(
    path: String
  )(implicit spark: SparkSession): DataFrame = {
    val loaded = spark.read
      .option("rowTag", "page")
      .xml(path)
      .filter(row => filterMetaArticles(row))
      .select("revision.text._VALUE", "title")
      .withColumnRenamed("_VALUE", "text")

    extractSections(loaded, sectionNames).drop("text")
  }

  def loadSimple(filename: String)(implicit spark: SparkSession): DataFrame =
    loadWiktionaryDump(simpleWiktionaryBucket + filename)

  def filterMetaArticles(row: Row): Boolean = {
    val title = row.getAs[String]("title")
    ignoredTitles.forall(prefix => !title.startsWith(prefix))
  }

  def getSections(data: DataFrame): DataFrame = {
    data.withColumn("sections", getSections(col("text")))
  }
  val getSections: UserDefinedFunction = udf(
    (text: String) =>
      text.linesIterator
        .filter(line => line.matches("=+ [A-Za-z0-9]+ =+.*"))
        .map(_.replaceAll("=+", ""))
        .toArray
  )

  val oneOrMoreEqualsSign = "=+"
  val optionalWhiteSpace = " *"
  val anythingButEqualsSign = "[^=]*"
  def sectionRegex(sectionName: String): String =
    oneOrMoreEqualsSign + optionalWhiteSpace + sectionName + optionalWhiteSpace + oneOrMoreEqualsSign + s"($anythingButEqualsSign)" // Capture group

  def extractSection(data: DataFrame, name: String): DataFrame =
    data.withColumn(
      name.toLowerCase(),
      regexp_extract(col("text"), sectionRegex(name), 0)
    )

  def extractSections(data: DataFrame, sections: List[String]): DataFrame = {
    sections.foldLeft(data)((data, section) => extractSection(data, section))
  }
}
