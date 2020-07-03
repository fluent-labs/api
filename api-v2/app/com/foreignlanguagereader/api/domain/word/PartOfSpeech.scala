package com.foreignlanguagereader.api.domain.word

import com.foreignlanguagereader.api.domain.word
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
  val ADPOSITION: Value = Value("Adposition")
  val ADVERB: Value = Value("Adverb")
  val CONJUNCTION: Value = Value("Conjunction")
  val DETERMINER: Value = Value("Determiner")
  val NOUN: Value = Value("Noun")
  val NUMBER: Value = Value("Number")
  val PRONOUN: Value = Value("Pronoun")
  val PARTICLE: Value = Value("Particle")
  val PUNCTUATION: Value = Value("Punctuation")
  val VERB: Value = Value("Verb")
  val OTHER: Value = Value("Other")
  val AFFIX: Value = Value("Affix")

  implicit val reads: Reads[word.PartOfSpeech.Value] =
    Reads.enumNameReads(PartOfSpeech)
  implicit val writes = Writes.enumNameWrites
}
