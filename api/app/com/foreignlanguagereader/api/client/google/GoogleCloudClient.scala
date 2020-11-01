package com.foreignlanguagereader.api.client.google

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.word.Count.Count
import com.foreignlanguagereader.domain.internal.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.domain.internal.word.WordTense.WordTense
import com.foreignlanguagereader.domain.internal.word.{PartOfSpeech, _}
import com.google.cloud.language.v1.Document.Type
import com.google.cloud.language.v1.PartOfSpeech.{
  Gender,
  Number,
  Proper,
  Tag,
  Tense
}
import com.google.cloud.language.v1.{
  AnalyzeSyntaxRequest,
  Document,
  EncodingType,
  Token
}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class GoogleCloudClient @Inject() (
    gcloud: GoogleLanguageServiceClientHolder,
    implicit val ec: ExecutionContext,
    val system: ActorSystem
) extends Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)

  def getWordsForDocument(
      language: Language,
      document: String
  ): Nested[Future, CircuitBreakerResult, Set[Word]] = {
    logger.info(s"Getting tokens in $language from Google cloud: $document")

    val doc =
      Document.newBuilder
        .setContent(document)
        .setType(Type.PLAIN_TEXT)
        .setLanguage(convertDomainLanguageToGoogleLanguage(language))
        .build

    val request = AnalyzeSyntaxRequest.newBuilder
      .setDocument(doc)
      .setEncodingType(EncodingType.UTF16)
      .build

    withBreaker(
      s"Failed to get tokens from google cloud",
      Future {
        gcloud.getTokens(request)
      }
    ).map(tokens => convertTokensToWord(language, tokens))
  }

  /*
   * Converter functions that go between the google model and our domain model
   *
   * These are documented here: https://cloud.google.com/natural-language/docs/reference/rest/v1/Token
   */

  def convertDomainLanguageToGoogleLanguage(language: Language): String =
    language match {
      case Language.CHINESE             => "zh"
      case Language.CHINESE_TRADITIONAL => "zh-Hant"
      case Language.ENGLISH             => "en"
      case Language.SPANISH             => "es"
    }

  def convertTokensToWord(language: Language, tokens: Seq[Token]): Set[Word] =
    tokens
      .map(token =>
        Word(
          language = language,
          token = token.getText.getContent,
          tag = googlePartOfSpeechToDomainPartOfSpeech(
            token.getPartOfSpeech.getTag
          ),
          lemma = token.getLemma,
          definitions = List(),
          gender = googleGenderToDomainGender(token.getPartOfSpeech.getGender),
          number = googleCountToDomainCount(token.getPartOfSpeech.getNumber),
          proper = isProperNoun(token.getPartOfSpeech.getProper),
          tense = googleTenseToDomainTense(token.getPartOfSpeech.getTense),
          processedToken = token.getText.getContent
        )
      )
      .toSet

  def googlePartOfSpeechToDomainPartOfSpeech(tag: Tag): PartOfSpeech =
    tag match {
      case Tag.ADJ   => PartOfSpeech.ADJECTIVE
      case Tag.ADP   => PartOfSpeech.ADPOSITION
      case Tag.ADV   => PartOfSpeech.ADVERB
      case Tag.CONJ  => PartOfSpeech.CONJUNCTION
      case Tag.DET   => PartOfSpeech.DETERMINER
      case Tag.NOUN  => PartOfSpeech.NOUN
      case Tag.NUM   => PartOfSpeech.NUMBER
      case Tag.PRON  => PartOfSpeech.PRONOUN
      case Tag.PRT   => PartOfSpeech.PARTICLE
      case Tag.PUNCT => PartOfSpeech.PUNCTUATION
      case Tag.VERB  => PartOfSpeech.VERB
      case Tag.X     => PartOfSpeech.OTHER
      case Tag.AFFIX => PartOfSpeech.AFFIX
      case _         => PartOfSpeech.UNKNOWN
    }

  def googleGenderToDomainGender(gender: Gender): Option[GrammaticalGender] =
    gender match {
      case Gender.FEMININE  => Some(GrammaticalGender.FEMALE)
      case Gender.MASCULINE => Some(GrammaticalGender.MALE)
      case Gender.NEUTER    => Some(GrammaticalGender.NEUTER)
      // This covers both parsing failures and languages which don't have gender
      case _ => None
    }

  def googleCountToDomainCount(number: Number): Option[Count] =
    number match {
      case Number.SINGULAR => Some(Count.SINGLE)
      case Number.PLURAL   => Some(Count.PLURAL)
      case Number.DUAL     => Some(Count.DUAL)
      case _               => None
    }

  def isProperNoun(proper: Proper): Option[Boolean] =
    proper match {
      case Proper.PROPER     => Some(true)
      case Proper.NOT_PROPER => Some(false)
      case _                 => None
    }

  def googleTenseToDomainTense(tense: Tense): Option[WordTense] =
    tense match {
      case Tense.CONDITIONAL_TENSE => Some(WordTense.CONDITIONAL)
      case Tense.PAST              => Some(WordTense.PAST)
      case Tense.PRESENT           => Some(WordTense.PRESENT)
      case Tense.FUTURE            => Some(WordTense.FUTURE)
      case Tense.IMPERFECT         => Some(WordTense.IMPERFECT)
      case Tense.PLUPERFECT        => Some(WordTense.PLUPERFECT)
      case _                       => None
    }
}
