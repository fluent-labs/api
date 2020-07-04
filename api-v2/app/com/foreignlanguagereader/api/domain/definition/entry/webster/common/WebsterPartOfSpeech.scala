package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import com.foreignlanguagereader.api.domain.word.PartOfSpeech
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json.{Reads, Writes}

object WebsterPartOfSpeech extends Enumeration {
  type WebsterPartOfSpeech = Value
  val ABBREVIATION: Value = Value("abbreviation")
  val ADJECTIVE: Value = Value("adjective")
  val ADVERB: Value = Value("adverb")
  val CONJUNCTION: Value = Value("conjunction")
  val INTERJECTION: Value = Value("interjection")
  val NOUN: Value = Value("noun")
  val PREPOSITION: Value = Value("preposition")
  val PRONOUN: Value = Value("pronoun")
  val VERB: Value = Value("verb")

  implicit val reads: Reads[WebsterPartOfSpeech] =
    Reads.enumNameReads(WebsterPartOfSpeech)
  implicit val writes: Writes[WebsterPartOfSpeech] = Writes.enumNameWrites

  // Webster's model of part of speech varies a bit from ours. This is how we go between them.
  def toDomain(partOfSpeech: WebsterPartOfSpeech): PartOfSpeech =
    partOfSpeech match {
      // Obvious mappings
      case WebsterPartOfSpeech.ADJECTIVE   => PartOfSpeech.ADJECTIVE
      case WebsterPartOfSpeech.ADVERB      => PartOfSpeech.ADVERB
      case WebsterPartOfSpeech.CONJUNCTION => PartOfSpeech.CONJUNCTION
      case WebsterPartOfSpeech.NOUN        => PartOfSpeech.NOUN
      case WebsterPartOfSpeech.PRONOUN     => PartOfSpeech.PRONOUN
      case WebsterPartOfSpeech.VERB        => PartOfSpeech.VERB

      // The ones that needed interpretation
      case WebsterPartOfSpeech.ABBREVIATION => PartOfSpeech.OTHER
      case WebsterPartOfSpeech.INTERJECTION => PartOfSpeech.PARTICLE
      case WebsterPartOfSpeech.PREPOSITION  => PartOfSpeech.ADPOSITION
    }
}
