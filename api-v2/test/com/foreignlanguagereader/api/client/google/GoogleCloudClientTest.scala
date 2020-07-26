package com.foreignlanguagereader.api.client.google

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.word.GrammaticalGender
import com.foreignlanguagereader.api.domain.word.{PartOfSpeech, _}
import com.google.cloud.language.v1.AnalyzeSyntaxRequest
import com.google.cloud.language.v1.PartOfSpeech.{
  Gender,
  Number,
  Proper,
  Tag,
  Tense
}
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

class GoogleCloudClientTest extends AsyncFunSpec with MockitoSugar {
  val holderMock: GoogleLanguageServiceClientHolder =
    mock[GoogleLanguageServiceClientHolder]

  val client = new GoogleCloudClient(
    holderMock,
    scala.concurrent.ExecutionContext.Implicits.global,
    ActorSystem("testActorSystem", ConfigFactory.load())
  )

  describe("A google cloud language client") {
    describe("When getting tokens") {
      // No happy path test unfortunately.
      // Google has done a really good job making sure I can't create their domain objects.
      // It might be possible to use reflection to create some return values
      // But the return on effort is pretty low.

      it("can handle if no tokens are returned") {
        when(holderMock.getTokens(any[AnalyzeSyntaxRequest]))
          .thenReturn(List())

        client
          .getWordsForDocument(Language.ENGLISH, "")
          .map(result => assert(result.isEmpty))
      }

      it("returns none if an exception is thrown") {
        when(holderMock.getTokens(any[AnalyzeSyntaxRequest]))
          .thenThrow(new IllegalArgumentException("Uh oh"))

        client
          .getWordsForDocument(Language.ENGLISH, "")
          .map(result => assert(result.isEmpty))
      }
    }

    describe("when converting between google and domain types") {
      it("can correctly convert languages") {
        assert(
          client.convertDomainLanguageToGoogleLanguage(Language.CHINESE) == "zh"
        )
        assert(
          client.convertDomainLanguageToGoogleLanguage(
            Language.CHINESE_TRADITIONAL
          ) == "zh-Hant"
        )
        assert(
          client.convertDomainLanguageToGoogleLanguage(Language.ENGLISH) == "en"
        )
        assert(
          client.convertDomainLanguageToGoogleLanguage(Language.SPANISH) == "es"
        )
      }

      it("can correctly convert tags") {
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.ADJ)
            .contains(PartOfSpeech.ADJECTIVE)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.ADP)
            .contains(PartOfSpeech.ADPOSITION)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.ADV)
            .contains(PartOfSpeech.ADVERB)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.CONJ)
            .contains(PartOfSpeech.CONJUNCTION)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.DET)
            .contains(PartOfSpeech.DETERMINER)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.NOUN)
            .contains(PartOfSpeech.NOUN)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.NUM)
            .contains(PartOfSpeech.NUMBER)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.PRON)
            .contains(PartOfSpeech.PRONOUN)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.PRT)
            .contains(PartOfSpeech.PARTICLE)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.PUNCT)
            .contains(PartOfSpeech.PUNCTUATION)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.VERB)
            .contains(PartOfSpeech.VERB)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.X)
            .contains(PartOfSpeech.OTHER)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.AFFIX)
            .contains(PartOfSpeech.AFFIX)
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.UNKNOWN)
            .isEmpty
        )
      }

      it("can correctly convert grammatical gender") {
        assert(
          client
            .googleGenderToDomainGender(Gender.MASCULINE)
            .contains(GrammaticalGender.MALE)
        )
        assert(
          client
            .googleGenderToDomainGender(Gender.FEMININE)
            .contains(GrammaticalGender.FEMALE)
        )
        assert(
          client
            .googleGenderToDomainGender(Gender.NEUTER)
            .contains(GrammaticalGender.NEUTER)
        )
        assert(
          client
            .googleGenderToDomainGender(Gender.UNRECOGNIZED)
            .isEmpty
        )
      }

      it("can correctly convert count") {
        assert(
          client
            .googleCountToDomainCount(Number.SINGULAR)
            .contains(Count.SINGLE)
        )
        assert(
          client
            .googleCountToDomainCount(Number.PLURAL)
            .contains(Count.PLURAL)
        )
        assert(
          client
            .googleCountToDomainCount(Number.DUAL)
            .contains(Count.DUAL)
        )
        assert(
          client
            .googleCountToDomainCount(Number.UNRECOGNIZED)
            .isEmpty
        )
      }

      it("can correctly convert proper nouns") {
        assert(
          client
            .isProperNoun(Proper.PROPER)
            .contains(true)
        )
        assert(
          client
            .isProperNoun(Proper.NOT_PROPER)
            .contains(false)
        )
        assert(
          client
            .isProperNoun(Proper.UNRECOGNIZED)
            .isEmpty
        )
      }

      it("can correctly convert tense") {
        assert(
          client
            .googleTenseToDomainTense(Tense.CONDITIONAL_TENSE)
            .contains(WordTense.CONDITIONAL)
        )
        assert(
          client
            .googleTenseToDomainTense(Tense.PAST)
            .contains(WordTense.PAST)
        )
        assert(
          client
            .googleTenseToDomainTense(Tense.PRESENT)
            .contains(WordTense.PRESENT)
        )
        assert(
          client
            .googleTenseToDomainTense(Tense.FUTURE)
            .contains(WordTense.FUTURE)
        )
        assert(
          client
            .googleTenseToDomainTense(Tense.IMPERFECT)
            .contains(WordTense.IMPERFECT)
        )
        assert(
          client
            .googleTenseToDomainTense(Tense.PLUPERFECT)
            .contains(WordTense.PLUPERFECT)
        )
        assert(
          client
            .googleTenseToDomainTense(Tense.UNRECOGNIZED)
            .isEmpty
        )
      }
    }
  }
}
