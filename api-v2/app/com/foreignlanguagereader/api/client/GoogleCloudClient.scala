package com.foreignlanguagereader.api.client

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.word.Count.Count
import com.foreignlanguagereader.api.domain.word.{
  Count,
  GrammaticalGender,
  PartOfSpeech,
  Word,
  WordTense
}
import com.foreignlanguagereader.api.domain.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.api.domain.word.WordTense.WordTense
import com.google.cloud.language.v1.Document.Type
import com.google.cloud.language.v1.PartOfSpeech.{
  Gender,
  Number,
  Proper,
  Tag,
  Tense
}
import com.google.cloud.language.v1._
import javax.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class GoogleCloudClient @Inject()(config: Configuration,
                                  val system: ActorSystem) {
  val logger: Logger = Logger(this.getClass)
  implicit val myExecutionContext: ExecutionContext =
    system.dispatchers.lookup("language-service-context")

  private[this] val gcloud = LanguageServiceClient.create()

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

    Future {
      Try(gcloud.analyzeSyntax(request).getTokensList.asScala.toList) match {
        case Success(tokens) => Some(convertTokensToWord(language, tokens))
        case Failure(e) =>
          logger.error(
            s"Failed to get tokens from google cloud: ${e.getMessage}",
            e
          )
          None
      }
    }
  }

  /*
   * Converter functions that go between the google model and our domain model
   *
   * These are documented here: https://cloud.google.com/natural-language/docs/reference/rest/v1/Token
   */

  private[this] def convertDomainLanguageToGoogleLanguage(
    language: Language
  ): String = language match {
    case Language.CHINESE             => "zh"
    case Language.CHINESE_TRADITIONAL => "zh-Hant"
    case Language.ENGLISH             => "en"
    case Language.SPANISH             => "es"
  }

  private[this] def convertTokensToWord(language: Language,
                                        tokens: Seq[Token]): Set[Word] =
    tokens
      .map(
        token =>
          Word(
            language,
            token.getText.getContent,
            googlePartOfSpeechToDomainPartOfSpeech(
              token.getPartOfSpeech.getTag
            ),
            token.getLemma,
            None,
            googleGenderToDomainGender(token.getPartOfSpeech.getGender),
            googleCountToDomainCount(token.getPartOfSpeech.getNumber),
            isProperNoun(token.getPartOfSpeech.getProper),
            googleTenseToDomainTense(token.getPartOfSpeech.getTense)
        )
      )
      .toSet

  private[this] def googlePartOfSpeechToDomainPartOfSpeech(
    tag: Tag
  ): Option[PartOfSpeech] = tag match {
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

  private[this] def googleGenderToDomainGender(
    gender: Gender
  ): Option[GrammaticalGender] = gender match {
    case Gender.FEMININE  => Some(GrammaticalGender.FEMALE)
    case Gender.MASCULINE => Some(GrammaticalGender.MALE)
    case Gender.NEUTER    => Some(GrammaticalGender.NEUTER)
    // This covers both parsing failures and languages which don't have gender
    case _ => None
  }

  private[this] def googleCountToDomainCount(number: Number): Option[Count] =
    number match {
      case Number.SINGULAR => Some(Count.SINGLE)
      case Number.PLURAL   => Some(Count.PLURAL)
      case Number.DUAL     => Some(Count.DUAL)
      case _               => None
    }

  private[this] def isProperNoun(proper: Proper): Option[Boolean] =
    proper match {
      case Proper.PROPER     => Some(true)
      case Proper.NOT_PROPER => Some(false)
      case _                 => None
    }

  private[this] def googleTenseToDomainTense(tense: Tense): Option[WordTense] =
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
