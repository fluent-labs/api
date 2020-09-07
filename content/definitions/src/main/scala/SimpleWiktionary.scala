import Wiktionary.{extractSections, loadWiktionaryDump}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}

case class SimpleWiktionaryDefinition(token: String,
                                      definition: String,
                                      tag: String,
                                      ipa: String) {
//  val subdefinitions: List[String]
  val examples: Option[List[String]] = None
  val definitionLanguage = "ENGLISH"
  val wordLanguage = "ENGLISH"
  val source = "WIKTIONARY_SIMPLE_ENGLISH"
}

object SimpleWiktionary {
  val metaSections = List("pronunciation", "usage", "usage notes")

  // Parts of speech set here: http://www.lrec-conf.org/proceedings/lrec2012/pdf/274_Paper.pdf
  val partOfSpeechMapping: Map[String, String] = Map(
    "abbreviation" -> "Noun",
    "acronym" -> "Noun",
    "adjective" -> "Adjective",
    "adjective 1" -> "Adjective",
    "adverb" -> "Adverb",
    "auxiliary verb" -> "Verb",
    "compound determinative" -> "Determiner",
    "conjunction" -> "Conjunction",
    "contraction" -> "Unknown",
    "demonstrative determiner" -> "Determiner",
    "determinative" -> "Determiner",
    "determiner" -> "Determiner",
    "expression" -> "Other",
    "initialism" -> "Noun", // basically acronym
    "interjection" -> "Particle",
    "noun" -> "Noun",
    "noun 1" -> "Noun",
    "noun 2" -> "Noun",
    "noun 3" -> "Noun",
    "prefix" -> "Affix",
    "preposition" -> "Adposition",
    "pronoun" -> "Pronoun",
    "proper noun" -> "Noun",
    "suffix" -> "Affix",
    "symbol" -> "Other",
    "verb" -> "Verb",
    "verb 1" -> "Verb",
    "verb 2" -> "Verb"
  )

  val partsOfSpeech: Array[String] = partOfSpeechMapping.keySet.toArray

  val subsectionMap: Map[String, String] = Map(
    "abbreviation" -> "other spellings",
    "alternate spellings" -> "other spellings",
    "alternative forms" -> "other spellings",
    "alternative spellings" -> "other spellings",
    "antonym" -> "antonyms",
    "antonyms" -> "antonyms",
    "homonyms" -> "homonyms",
    "homophone" -> "homophones",
    "homophones" -> "homophones",
    "note" -> "notes",
    "notes" -> "notes",
    "notes of usage" -> "notes",
    "other spelling" -> "other spellings",
    "other spellings" -> "other spellings",
    "pronounciation" -> "pronunciation",
    "pronunciation" -> "pronunciation",
    "pronunciation 2" -> "pronunciation",
    "related" -> "related",
    "related old words" -> "related",
    "related phrases" -> "related",
    "related terms" -> "related",
    "related word" -> "related",
    "related word and phrases" -> "related",
    "related words" -> "related",
    "related words and expressions" -> "related",
    "related words and phrases" -> "related",
    "related words and terms" -> "related",
    "related words or phrases" -> "related",
    "related words/phrases" -> "related",
    "see also" -> "related",
    "synonym" -> "synonyms",
    "synonyms" -> "synonyms",
    "usage" -> "usage",
    "usage note" -> "usage",
    "usage notes" -> "usage",
    "verb usage" -> "usage"
  )

  val partOfSpeechCols: Column =
    array(partsOfSpeech.head, partsOfSpeech.tail: _*)

  def mapWiktionaryPartOfSpeechToDomainPartOfSpeech(
    partOfSpeech: String
  ): String = partOfSpeechMapping.getOrElse(partOfSpeech, "Unknown")

  val leftBracket = "\\{"
  val pipe = "\\|"
  val rightBracket = "\\}"
  val slash = "\\/"
  val anythingButSlash = "([^\\/]+)"
  // It looks like this: {{IPA|/whatWeWant/}}
  val ipaRegex
    : String = leftBracket + leftBracket + "IPA" + pipe + slash + anythingButSlash + slash + rightBracket + rightBracket

  def loadSimple(
    path: String
  )(implicit spark: SparkSession): Dataset[SimpleWiktionaryDefinition] = {
    import spark.implicits._
    splitWordsByPartOfSpeech(loadWiktionaryDump(path))
      .withColumn("ipa", regexp_extract(col("text"), ipaRegex, 1))
      .as[SimpleWiktionaryDefinition]
  }

  val mapPartOfSpeech: UserDefinedFunction = udf(
    (index: Integer) =>
      mapWiktionaryPartOfSpeechToDomainPartOfSpeech(partsOfSpeech(index))
  )

  def splitWordsByPartOfSpeech(data: DataFrame): DataFrame =
    extractSections(data, SimpleWiktionary.partsOfSpeech)
      .select(col("token"), col("text"), posexplode(partOfSpeechCols))
      .filter("col not like ''")
      .drop(partOfSpeechCols)
      .withColumnRenamed("col", "definition")
      .withColumn("tag", mapPartOfSpeech(col("pos")))
      .drop("pos")
}
