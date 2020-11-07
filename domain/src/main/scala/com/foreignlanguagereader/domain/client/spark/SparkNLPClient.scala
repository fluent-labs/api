package com.foreignlanguagereader.domain.client.spark

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech._
import com.foreignlanguagereader.content.types.internal.word.Word
import com.johnsnowlabs.nlp.LightPipeline
import org.apache.spark.sql.SparkSession
import play.api.Logger

class SparkNLPClient {
  implicit val spark: SparkSession = NLPSparkContext.spark
  val logger: Logger = Logger(this.getClass)

  val chineseModel: LightPipeline =
    NLPPipeline.makePipeline(
      Language.CHINESE,
      "lemma_zh_2.5.0_2.4_1588611649140",
      "pos_ud_gsd_zh_2.5.0_2.4_1588611712161"
    )

  val englishModel: LightPipeline =
    NLPPipeline.makePipeline(
      Language.ENGLISH,
      "lemma_antbnc_en_2.0.2_2.4_1556480454569",
      "pos_ud_ewt_en_2.2.2_2.4_1570464827452"
    )
  val spanishModel: LightPipeline =
    NLPPipeline.makePipeline(
      Language.SPANISH,
      "lemma_es_2.4.0_2.4_1581890818386",
      "pos_ud_gsd_es_2.4.0_2.4_1581891015986"
    )

  def lemmatize(language: Language, text: String): Set[Word] = {
    val output: Map[String, Seq[String]] = (language match {
      case Language.CHINESE             => chineseModel.annotate(Array(text))
      case Language.CHINESE_TRADITIONAL => chineseModel.annotate(Array(text))
      case Language.ENGLISH             => englishModel.annotate(Array(text))
      case Language.SPANISH             => spanishModel.annotate(Array(text))
    }).head
    if (
      output.contains("token") && output
        .contains("lemmas") && output.contains("pos")
    ) {
      output
        .getOrElse("token", List())
        .zip(output.getOrElse("lemmas", List()))
        .zip(output.getOrElse("pos", List()))
        .map {
          case ((token, lemma), partOfSpeech) =>
            Word(
              language,
              token,
              modelPartOfSpeechToDomainPartOfSpeech(partOfSpeech),
              lemma,
              List(),
              None,
              None,
              None,
              None,
              token
            )
        }
        .toSet
    } else {
      logger.error(
        s"Failed to process input in $language because required fields (tokens, lemmas, pos) are missing - fields: ${output.keys} text:$text"
      )
      Set()
    }
  }

  def modelPartOfSpeechToDomainPartOfSpeech(pos: String): PartOfSpeech =
    pos match {
      case "ADJ"   => ADJECTIVE
      case "ADP"   => ADPOSITION
      case "ADV"   => ADVERB
      case "AUX"   => AUXILIARY
      case "CCONJ" => CONJUNCTION
      case "DET"   => DETERMINER
      case "INTJ"  => PARTICLE
      case "NOUN"  => NOUN
      case "NUM"   => NUMBER
      case "PART"  => PARTICLE
      case "PRON"  => PRONOUN
      case "PROPN" => PROPERNOUN
      case "PUNCT" => PUNCTUATION
      case "SCONJ" => CONJUNCTION
      case "SYM"   => OTHER
      case "VERB"  => VERB
      case "X"     => UNKNOWN
      case a =>
        logger.error(s"Unknown part of speech returned from Spark NLP: $a")
        UNKNOWN
    }
}
