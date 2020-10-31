package com.foreignlanguagereader.dto.v1.word

import play.api.libs.json.{Reads, Writes}
import sangria.macros.derive.{EnumTypeDescription, EnumTypeName, deriveEnumType}
import sangria.schema.EnumType

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
object PartOfSpeechDTO extends Enumeration {
  type PartOfSpeechDTO = Value
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

  implicit val reads: Reads[PartOfSpeechDTO] =
    Reads.enumNameReads(PartOfSpeechDTO)
  implicit val writes: Writes[PartOfSpeechDTO] = Writes.enumNameWrites

  implicit val graphqlType: EnumType[PartOfSpeechDTO] =
    deriveEnumType[PartOfSpeechDTO](
      EnumTypeName("PartOfSpeech"),
      EnumTypeDescription("The part of speech for a word")
    )
}
