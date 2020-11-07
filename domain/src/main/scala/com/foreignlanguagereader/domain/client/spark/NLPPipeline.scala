package com.foreignlanguagereader.domain.client.spark

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.domain.client.spark.SparkNLPClient.{
  getClass,
  logger
}
import com.johnsnowlabs.nlp.annotator.PerceptronModel
import com.johnsnowlabs.nlp.annotators.{LemmatizerModel, Tokenizer}
import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher, LightPipeline}
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.SparkSession

object NLPPipeline {
  def getModelPath(language: Language, modelName: String): String = {
    val path = s"/nlp/${language.toString.toLowerCase}/$modelName"
    logger.info(
      s"Loading $language NLP model $modelName from resource path $path"
    )
    getClass.getResource(path).getPath
  }

  def makePipeline(
      language: Language,
      lemmatizerModelName: String,
      partOfSpeechModelName: String
  )(implicit spark: SparkSession): LightPipeline = {
    val assembler = new DocumentAssembler()
      .setInputCol("text")
      .setOutputCol("document")
    val tokenizer = new Tokenizer()
      .setInputCols("document")
      .setOutputCol("token")
    val finisher = new Finisher()
      .setInputCols(Array("token", "lemmas", "pos"))
      .setIncludeMetadata(false)

    val lemmatizer = LemmatizerModel
      .load(getModelPath(language, lemmatizerModelName))
      .setInputCols(Array("token"))
      .setOutputCol("lemmas")

    val partOfSpeechTagger = PerceptronModel
      .load(getModelPath(language, partOfSpeechModelName))
      .setInputCols(Array("document", "token"))
      .setOutputCol("pos")

    val pipeline = new Pipeline()
      .setStages(
        Array(assembler, tokenizer, lemmatizer, partOfSpeechTagger, finisher)
      )

    import spark.implicits._
    val data = Seq("anything").toDF("text")
    new LightPipeline(pipeline.fit(data))
  }
}
