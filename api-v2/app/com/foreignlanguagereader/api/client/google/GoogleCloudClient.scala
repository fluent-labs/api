package com.foreignlanguagereader.api.client.google

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerNonAttempt,
  Circuitbreaker
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.word.Count.Count
import com.foreignlanguagereader.api.domain.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.api.domain.word.WordTense.WordTense
import com.foreignlanguagereader.api.domain.word.{PartOfSpeech, _}
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
import scala.util.{Failure, Success}

class GoogleCloudClient @Inject()(gcloud: GoogleLanguageServiceClientHolder,
                                  implicit val ec: ExecutionContext,
                                  val system: ActorSystem)
    extends Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)

  def getWordsForDocument(language: Language,
                          document: String): Future[Option[Set[Word]]] = {
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

    withBreaker(Future { gcloud.getTokens(request) }) map {
      case Success(CircuitBreakerAttempt(t)) =>
        t match {
          case List() =>
            logger.info(
              s"No tokens found in language=$language for document=$document"
            )
            None
          case tokens =>
            logger.info(
              s"Found tokens in language=$language for document=$document"
            )
            Some(convertTokensToWord(language, tokens))
        }
      case Success(CircuitBreakerNonAttempt()) => None
      case Failure(e) =>
        logger.error(
          s"Failed to get tokens from google cloud: ${e.getMessage}",
          e
        )
        None
    }
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
      .map(
        token =>
          Word(
            language = language,
            token = token.getText.getContent,
            tag = googlePartOfSpeechToDomainPartOfSpeech(
              token.getPartOfSpeech.getTag
            ),
            lemma = token.getLemma,
            definitions = None,
            gender = googleGenderToDomainGender(token.getPartOfSpeech.getGender),
            number = googleCountToDomainCount(token.getPartOfSpeech.getNumber),
            proper = isProperNoun(token.getPartOfSpeech.getProper),
            tense = googleTenseToDomainTense(token.getPartOfSpeech.getTense)
        )
      )
      .toSet

  def googlePartOfSpeechToDomainPartOfSpeech(tag: Tag): Option[PartOfSpeech] =
    tag match {
      case Tag.ADJ   => Some(PartOfSpeech.ADJECTIVE)
      case Tag.ADP   => Some(PartOfSpeech.ADPOSITION)
      case Tag.ADV   => Some(PartOfSpeech.ADVERB)
      case Tag.CONJ  => Some(PartOfSpeech.CONJUNCTION)
      case Tag.DET   => Some(PartOfSpeech.DETERMINER)
      case Tag.NOUN  => Some(PartOfSpeech.NOUN)
      case Tag.NUM   => Some(PartOfSpeech.NUMBER)
      case Tag.PRON  => Some(PartOfSpeech.PRONOUN)
      case Tag.PRT   => Some(PartOfSpeech.PARTICLE)
      case Tag.PUNCT => Some(PartOfSpeech.PUNCTUATION)
      case Tag.VERB  => Some(PartOfSpeech.VERB)
      case Tag.X     => Some(PartOfSpeech.OTHER)
      case Tag.AFFIX => Some(PartOfSpeech.AFFIX)
      case _         => None
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
