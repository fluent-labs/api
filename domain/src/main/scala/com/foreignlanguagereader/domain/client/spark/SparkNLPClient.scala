package com.foreignlanguagereader.domain.client.spark

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import com.johnsnowlabs.nlp.LightPipeline
import org.apache.spark.sql.SparkSession
import play.api.Logger

object SparkNLPClient {
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

  def lemmatize(language: Language, text: String): Unit = {
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
      (output.get("token"), output.get("lemmas"), output.get("pos")).zipped.map {
        case (token, lemma, partOfSpeech) => Word(language, token, )
      }
    }
  }

  def lemmatizeEnglish(sentence: String): Unit = {
    val whatever = englishModel
      .annotate(Array(sentence))
    whatever.head.foreach {
      case (key, values) =>
        val vals = values.mkString(",")
        println(s"$key: $vals")
    }
    print(whatever.head)
  }
}
