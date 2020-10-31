package com.foreignlanguagereader.domain.internal.word

import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO
import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO.PartOfSpeechDTO
import play.api.libs.json.{Reads, Writes}

/**
  * This is a crude bucketing of all possible parts of speech in a language.
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
  val CONJUNCTION: Value = Value("Conjunction")
  val DETERMINER: Value = Value("Determiner")
  val NOUN: Value = Value("Noun")
  val NUMBER: Value = Value("Number")
  val PRONOUN: Value = Value("Pronoun")
  // Particles are a bit of a grab bag. Interjections are a big part.
  val PARTICLE: Value = Value("Particle")
  val PUNCTUATION: Value = Value("Punctuation")
  val VERB: Value = Value("Verb")
  val OTHER: Value = Value("Other")
  val AFFIX: Value = Value("Affix")
  val UNKNOWN: Value = Value("Unknown")

  implicit val reads: Reads[PartOfSpeech] = Reads.enumNameReads(PartOfSpeech)
  implicit val writes: Writes[PartOfSpeech] = Writes.enumNameWrites

  // scalastyle:off cyclomatic.complexity
  def toDTO(partOfSpeech: PartOfSpeech): PartOfSpeechDTO =
    partOfSpeech match {
      case ADJECTIVE   => PartOfSpeechDTO.ADJECTIVE
      case ADPOSITION  => PartOfSpeechDTO.ADPOSITION
      case ADVERB      => PartOfSpeechDTO.ADVERB
      case CONJUNCTION => PartOfSpeechDTO.CONJUNCTION
      case DETERMINER  => PartOfSpeechDTO.DETERMINER
      case NOUN        => PartOfSpeechDTO.NOUN
      case NUMBER      => PartOfSpeechDTO.NUMBER
      case PRONOUN     => PartOfSpeechDTO.PRONOUN
      case PARTICLE    => PartOfSpeechDTO.PARTICLE
      case PUNCTUATION => PartOfSpeechDTO.PUNCTUATION
      case VERB        => PartOfSpeechDTO.VERB
      case OTHER       => PartOfSpeechDTO.OTHER
      case AFFIX       => PartOfSpeechDTO.AFFIX
      case UNKNOWN     => PartOfSpeechDTO.UNKNOWN
    }
  // scalastyle:on cyclomatic.complexity
}
