package io.fluentlabs.domain.client.google

import akka.actor.ActorSystem
import io.fluentlabs.content.types.Language
import io.fluentlabs.content.types.internal.word.{
  Count,
  GrammaticalGender,
  PartOfSpeech,
  WordTense
}
import com.google.cloud.language.v1.AnalyzeSyntaxRequest
import com.google.cloud.language.v1.PartOfSpeech.{
  Gender,
  Number,
  Proper,
  Tag,
  Tense
}
import com.typesafe.config.ConfigFactory
import io.fluentlabs.domain.client.circuitbreaker.CircuitBreakerAttempt
import io.fluentlabs.domain.metrics.MetricsReporter
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

class GoogleCloudClientTest extends AsyncFunSpec with MockitoSugar {
  val holderMock: GoogleLanguageServiceClientHolder =
    mock[GoogleLanguageServiceClientHolder]
  val metricsMock: MetricsReporter = mock[MetricsReporter]

  val client = new GoogleCloudClient(
    holderMock,
    metricsMock,
    scala.concurrent.ExecutionContext.Implicits.global,
    ActorSystem("testActorSystem", ConfigFactory.load())
  )

  describe("A google cloud language client") {
    describe("when querying google cloud") {
      it("can handle the happy path") {
        when(holderMock.getTokens(any(classOf[AnalyzeSyntaxRequest])))
          .thenReturn(List())

        client
          .getWordsForDocument(Language.ENGLISH, "test document")
          .map {
            case CircuitBreakerAttempt(result) => assert(result.isEmpty)
            case _                             => fail("This isn't the happy path")
          }
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
            == PartOfSpeech.ADJECTIVE
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.ADP)
            == PartOfSpeech.ADPOSITION
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.ADV)
            == PartOfSpeech.ADVERB
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.CONJ)
            == PartOfSpeech.CONJUNCTION
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.DET)
            == PartOfSpeech.DETERMINER
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.NOUN)
            == PartOfSpeech.NOUN
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.NUM)
            == PartOfSpeech.NUMBER
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.PRON)
            == PartOfSpeech.PRONOUN
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.PRT)
            == PartOfSpeech.PARTICLE
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.PUNCT)
            == PartOfSpeech.PUNCTUATION
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.VERB)
            == PartOfSpeech.VERB
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.X)
            == PartOfSpeech.OTHER
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.AFFIX)
            == PartOfSpeech.ADJECTIVE
        )
        assert(
          client
            .googlePartOfSpeechToDomainPartOfSpeech(Tag.UNKNOWN)
            == PartOfSpeech.UNKNOWN
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
