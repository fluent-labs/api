import Wiktionary.{extractSections, loadWiktionaryDump}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, Dataset, SparkSession}

case class SimpleWiktionaryDefinition(token: String,
                                      definition: String,
                                      tag: String) {
//  val subdefinitions: List[String]
//  val ipa: String
//  val examples: Option[List[String]]
  val definitionLanguage = "ENGLISH"
  val wordLanguage = "ENGLISH"
  val source = "WIKTIONARY_SIMPLE_ENGLISH"
}

object SimpleWiktionary {
  val partsOfSpeech: Array[String] = Array(
    "abbreviation",
    "acronym",
    "adjective",
    "adjective 1",
    "adverb",
    "auxiliary verb",
    "compound determinative",
    "conjunction",
    "contraction",
    "demonstrative determiner",
    "determinative",
    "determiner",
    "expression",
    "initialism", // basically acronym
    "interjection",
    "noun",
    "noun 1",
    "noun 2",
    "noun 3",
    "prefix",
    "preposition",
    "pronoun",
    "proper noun",
    "suffix",
    "symbol",
    "verb",
    "verb 1",
    "verb 2"
  )

  val metaSections = List("pronunciation", "usage", "usage notes")

  // Parts of speech set here: http://www.lrec-conf.org/proceedings/lrec2012/pdf/274_Paper.pdf
  def mapWiktionaryPartOfSpeechToDomainPartOfSpeech(
    partOfSpeech: String
  ): String = partOfSpeech match {
    case "abbreviation"             => "Noun"
    case "acronym"                  => "Noun"
    case "adjective"                => "Adjective"
    case "adjective 1"              => "Adjective"
    case "adverb"                   => "Adverb"
    case "auxiliary verb"           => "Verb"
    case "compound determinative"   => "Determiner"
    case "conjunction"              => "Conjunction"
    case "contraction"              => "Unknown"
    case "demonstrative determiner" => "Determiner"
    case "determinative"            => "Determiner"
    case "determiner"               => "Determiner"
    case "expression"               => "Other"
    case "initialism"               => "Noun"
    case "interjection"             => "Particle"
    case "noun"                     => "Noun"
    case "noun 1"                   => "Noun"
    case "noun 2"                   => "Noun"
    case "noun 3"                   => "Noun"
    case "prefix"                   => "Affix"
    case "preposition"              => "Adposition"
    case "pronoun"                  => "Pronoun"
    case "proper noun"              => "Noun"
    case "suffix"                   => "Affix"
    case "symbol"                   => "Other"
    case "verb"                     => "Verb"
    case "verb 1"                   => "Verb"
    case "verb 2"                   => "Verb"
  }

  val partOfSpeechCols: Column =
    array(partsOfSpeech.head, partsOfSpeech.tail: _*)

  def loadSimple(
    path: String
  )(implicit spark: SparkSession): Dataset[SimpleWiktionaryDefinition] = {
    import spark.implicits._
    val sectioned =
      extractSections(loadWiktionaryDump(path), SimpleWiktionary.partsOfSpeech)
    sectioned
      .select(col("token"), posexplode(partOfSpeechCols))
      .filter("col not like ''")
      .drop(partOfSpeechCols)
      .withColumnRenamed("col", "definition")
      .withColumn("tag", mapPartOfSpeech(col("pos")))
      .drop("pos")
      .as[SimpleWiktionaryDefinition]
  }

  val mapPartOfSpeech: UserDefinedFunction = udf(
    (index: Integer) =>
      mapWiktionaryPartOfSpeechToDomainPartOfSpeech(partsOfSpeech(index))
  )
}
