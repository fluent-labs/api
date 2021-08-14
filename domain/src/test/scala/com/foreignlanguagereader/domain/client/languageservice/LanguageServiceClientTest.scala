package com.foreignlanguagereader.domain.client.languageservice

import io.fluentlabs.content.types.internal.word.PartOfSpeech
import org.scalatest.funspec.AsyncFunSpec

class LanguageServiceClientTest extends AsyncFunSpec {

  describe("A language service client") {
    it("can convert language service parts of speech to domain ones") {
      val assertions = Map(
        "ADJ" -> PartOfSpeech.ADJECTIVE,
        "ADP" -> PartOfSpeech.ADPOSITION,
        "ADV" -> PartOfSpeech.ADVERB,
        "AUX" -> PartOfSpeech.AUXILIARY,
        "CONJ" -> PartOfSpeech.CONJUNCTION,
        "CCONJ" -> PartOfSpeech.COORDINATING_CONJUNCTION,
        "DET" -> PartOfSpeech.DETERMINER,
        "INTJ" -> PartOfSpeech.INTERJECTION,
        "NOUN" -> PartOfSpeech.NOUN,
        "NUM" -> PartOfSpeech.NUMBER,
        "PART" -> PartOfSpeech.PARTICLE,
        "PRON" -> PartOfSpeech.PRONOUN,
        "PROPN" -> PartOfSpeech.PROPER_NOUN,
        "PUNCT" -> PartOfSpeech.PUNCTUATION,
        "SCONJ" -> PartOfSpeech.SUBORDINATING_CONJUNCTION,
        "SYM" -> PartOfSpeech.SYMBOL,
        "VERB" -> PartOfSpeech.VERB,
        "X" -> PartOfSpeech.OTHER,
        "SPACE" -> PartOfSpeech.SPACE,
        "something else" -> PartOfSpeech.UNKNOWN
      )

      assertions.foreach { case (language, domain) =>
        assert(
          LanguageServiceClient
            .spacyPartOfSpeechToDomainPartOfSpeech(language) == domain
        )
      }
      succeed
    }
  }
}
