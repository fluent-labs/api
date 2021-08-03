package com.foreignlanguagereader.content.types.internal.word

import cats.syntax.all._
import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO
import play.api.libs.json.{Reads, Writes}

/** This is a crude bucketing of all possible parts of speech in a language.
  *
  * All models are going to have a different set of parts of speech,
  * but we need a common set to work with.
  *
  * These are meant to be very general since our audience is language learners
  * who will be confused by too much detail.
  *
  * Exceptions can be made for things that are fundamental to one of our supported languages.
  * (Chinese measure words come to mind)
  */
object PartOfSpeech extends Enumeration {
  type PartOfSpeech = Value
  val ADJECTIVE: Value = Value("Adjective")
  // What's an adposition? Prepositions and postpositions
  val ADPOSITION: Value = Value("Adposition")
  val ADVERB: Value = Value("Adverb")
  val AUXILIARY: Value = Value("Auxiliary")
  val CONJUNCTION: Value = Value("Conjunction")
  val COORDINATING_CONJUNCTION: Value = Value("CoordinatingConjunction")
  val DETERMINER: Value = Value("Determiner")
  val INTERJECTION: Value = Value("Interjection")
  val NOUN: Value = Value("Noun")
  val NUMBER: Value = Value("Number")
  // Particles are a bit of a grab bag. Interjections are a big part.
  val PARTICLE: Value = Value("Particle")
  val PRONOUN: Value = Value("Pronoun")
  val PROPER_NOUN: Value = Value("ProperNoun")
  val PUNCTUATION: Value = Value("Punctuation")
  val SUBORDINATING_CONJUNCTION: Value = Value("SubordinatingConjunction")
  val SYMBOL: Value = Value("Symbol")
  val VERB: Value = Value("Verb")
  val OTHER: Value = Value("Other")
  val SPACE: Value = Value("Space")
  val UNKNOWN: Value = Value("Unknown")

  implicit val reads: Reads[PartOfSpeech] = Reads.enumNameReads(PartOfSpeech)
  implicit val writes: Writes[PartOfSpeech] = Writes.enumNameWrites

  def fromString(s: String): Option[PartOfSpeech] =
    PartOfSpeech.values.find(_.toString === s)

  // scalastyle:off cyclomatic.complexity
  def toDTO(partOfSpeech: PartOfSpeech): PartOfSpeechDTO =
    partOfSpeech match {
      case ADJECTIVE                => PartOfSpeechDTO.ADJECTIVE
      case ADPOSITION               => PartOfSpeechDTO.ADPOSITION
      case ADVERB                   => PartOfSpeechDTO.ADVERB
      case AUXILIARY                => PartOfSpeechDTO.AUXILIARY
      case CONJUNCTION              => PartOfSpeechDTO.CONJUNCTION
      case COORDINATING_CONJUNCTION => PartOfSpeechDTO.COORDINATING_CONJUNCTION
      case DETERMINER               => PartOfSpeechDTO.DETERMINER
      case INTERJECTION             => PartOfSpeechDTO.INTERJECTION
      case NOUN                     => PartOfSpeechDTO.NOUN
      case NUMBER                   => PartOfSpeechDTO.NUMBER
      case PARTICLE                 => PartOfSpeechDTO.PARTICLE
      case PRONOUN                  => PartOfSpeechDTO.PRONOUN
      case PROPER_NOUN              => PartOfSpeechDTO.PROPER_NOUN
      case PUNCTUATION              => PartOfSpeechDTO.PUNCTUATION
      case SYMBOL                   => PartOfSpeechDTO.SYMBOL
      case VERB                     => PartOfSpeechDTO.VERB
      case OTHER                    => PartOfSpeechDTO.OTHER
      case SPACE                    => PartOfSpeechDTO.SPACE
      case UNKNOWN                  => PartOfSpeechDTO.UNKNOWN
      case SUBORDINATING_CONJUNCTION =>
        PartOfSpeechDTO.SUBORDINATING_CONJUNCTION
    }
  // scalastyle:on cyclomatic.complexity
}
