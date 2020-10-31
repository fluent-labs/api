import Wiktionary.{
  extractSections,
  extractSubsections,
  loadWiktionaryDump,
  regexp_extract_all
}
import com.foreignlanguagereader.domain.external.definition.wiktionary.SimpleWiktionaryDefinitionEntry
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}

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
    "abbreviation" -> "otherSpellings",
    "alternate spellings" -> "otherSpellings",
    "alternative forms" -> "otherSpellings",
    "alternative spellings" -> "otherSpellings",
    "antonym" -> "antonyms",
    "antonyms" -> "antonyms",
    "homonyms" -> "homonyms",
    "homophone" -> "homophones",
    "homophones" -> "homophones",
    "note" -> "notes",
    "notes" -> "notes",
    "notes of usage" -> "notes",
    "other spelling" -> "otherSpellings",
    "other spellings" -> "otherSpellings",
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
  val optionalSpace = " *"
  val newline = "\\n"

  // It looks like this: {{IPA|/whatWeWant/}}
  val ipaRegex: String =
    leftBracket + leftBracket + "IPA" + pipe + slash + anythingButSlash + slash + rightBracket + rightBracket

  val subdefinitionMarker = "#"
  val examplesMarker = "#:"
  val subdefinitionsRegex: String =
    subdefinitionMarker + optionalSpace + "([^\\n:]*)" + newline
  val examplesRegex: String = examplesMarker + optionalSpace + "([^\\n]*)"

  def loadSimple(
      path: String
  )(implicit spark: SparkSession): Dataset[SimpleWiktionaryDefinitionEntry] = {
    parseSimple(loadWiktionaryDump(path))
  }

  def parseSimple(
      data: Dataset[WiktionaryRawEntry]
  )(implicit spark: SparkSession): Dataset[SimpleWiktionaryDefinitionEntry] = {
    import spark.implicits._
    val splitDefinitions = splitWordsByPartOfSpeech(data)
      .withColumn("ipa", regexp_extract(col("text"), ipaRegex, 1))
      .withColumn(
        "subdefinitions",
        regexp_extract_all(subdefinitionsRegex, 1)(col("definition"))
      )
      .withColumn(
        "examplesRaw",
        regexp_extract_all(examplesRegex, 1)(col("definition"))
      )

    addOptionalSections(splitDefinitions)
      .drop("text")
      .withColumnRenamed("pronunciation", "pronunciationRaw")
      .as[SimpleWiktionaryDefinitionEntry]
  }

  val mapPartOfSpeech: UserDefinedFunction = udf((index: Integer) =>
    mapWiktionaryPartOfSpeechToDomainPartOfSpeech(partsOfSpeech(index))
  )

  def splitWordsByPartOfSpeech(data: Dataset[WiktionaryRawEntry]): DataFrame =
    extractSections(data, SimpleWiktionary.partsOfSpeech)
      .select(col("token"), col("text"), posexplode(partOfSpeechCols))
      .filter("col not like ''")
      .drop(partOfSpeechCols)
      .withColumnRenamed("col", "definition")
      .withColumn("tagRaw", mapPartOfSpeech(col("pos")))
      .drop("pos")

  val subsectionsInverted: Map[String, Set[String]] = subsectionMap
    .groupBy(_._2)
    .mapValues(_.keySet)

  val subsectionsToDrop: Map[String, Set[String]] = subsectionsInverted.map {
    case (subsectionName, subsectionSet) =>
      // We don't want to lose the subsection we combined things to
      subsectionName -> subsectionSet.filter(!_.equals(subsectionName))
  }

  val subsectionsToCombine: Map[String, Column] =
    subsectionsInverted
      .mapValues(subsections =>
        array(subsections.head, subsections.tail.toArray: _*)
      )

  def addOptionalSections(data: DataFrame): DataFrame = {
    val extracted = extractSubsections(data, subsectionMap.keySet.toArray)
    subsectionsToCombine.foldLeft(extracted)((acc, subsection) => {
      val subsectionName = subsection._1
      val subsectionColumns = subsection._2
      val columnsToDrop: Array[String] =
        subsectionsToDrop.getOrElse(subsectionName, Set()).toArray
      acc
        .withColumn(subsectionName, array_remove(subsectionColumns, ""))
        .drop(columnsToDrop: _*)
    })
  }
}
