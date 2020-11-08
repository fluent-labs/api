package com.foreignlanguagereader.domain.client.spark

import cats.implicits._
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech._
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

class SparkNLPClientTest extends AsyncFunSpec with MockitoSugar {
  val client = new SparkNLPClient()

  describe("A spark NLP Client") {

    it("can process Chinese without error") {
      val result = client
        .lemmatize(Language.CHINESE, "看一下是不是一个好句子。")
        .toList
        .sortBy(_.token)
      val tokens = result.map(_.token)
      val lemmas = result.map(_.lemma)
      val tags = result.map(_.tag)

      assert(tokens === List("看一下是不是一个好句子。"))
      assert(lemmas === List("看一下是不是一个好句子。"))
      assert(tags === List(NOUN))
    }

    it("can process English") {
      val result =
        client
          .lemmatize(Language.ENGLISH, "This is a test sentence.")
          .toList
          .sortBy(_.token)
      val tokens = result.map(_.token)
      val lemmas = result.map(_.lemma)
      val tags = result.map(_.tag)

      assert(tokens === List(".", "This", "a", "is", "sentence", "test"))
      assert(lemmas === List(".", "This", "a", "be", "sentence", "test"))
      assert(
        tags === List(PUNCTUATION, PRONOUN, DETERMINER, AUXILIARY, NOUN, NOUN)
      )
    }

    it("can process Spanish") {
      val result = client
        .lemmatize(
          Language.SPANISH,
          "Yo quiero un taco al pastor con todo."
        )
        .toList
        .sortBy(_.token)
      val tokens = result.map(_.token)
      val lemmas = result.map(_.lemma)
      val tags = result.map(_.tag)

      assert(
        tokens ===
          List(
            ".",
            "Yo",
            "al",
            "con",
            "pastor",
            "quiero",
            "taco",
            "todo",
            "un"
          )
      )
      assert(
        lemmas ===
          List(
            ".",
            "Yo",
            "al",
            "con",
            "pastor",
            "querer",
            "taco",
            "todo",
            "uno"
          )
      )
      assert(
        tags === List(
          PUNCTUATION,
          PRONOUN,
          ADPOSITION,
          ADPOSITION,
          NOUN,
          VERB,
          NOUN,
          PRONOUN,
          DETERMINER
        )
      )
    }
  }
}
