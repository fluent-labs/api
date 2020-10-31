package com.foreignlanguagereader.domain.external.definition.webster.common

import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech.PartOfSpeech
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

  def parseFromString(partOfSpeech: String): Option[WebsterPartOfSpeech] = {
    // Special case - pronoun might match for noun.
    val (hasNoun, hasPronoun) =
      if (partOfSpeech.contains("pronoun")) (false, true)
      else (partOfSpeech.contains("noun"), false)

    val matches = List(
      if (partOfSpeech.contains("abbreviation"))
        Some(WebsterPartOfSpeech.ABBREVIATION)
      else None,
      if (partOfSpeech.contains("adjective"))
        Some(WebsterPartOfSpeech.ADJECTIVE)
      else None,
      if (partOfSpeech.contains("adverb"))
        Some(WebsterPartOfSpeech.ADVERB)
      else None,
      if (partOfSpeech.contains("conjunction"))
        Some(WebsterPartOfSpeech.CONJUNCTION)
      else None,
      if (partOfSpeech.contains("interjection"))
        Some(WebsterPartOfSpeech.INTERJECTION)
      else None,
      if (partOfSpeech.contains("preposition"))
        Some(WebsterPartOfSpeech.PREPOSITION)
      else None,
      if (partOfSpeech.contains("verb")) Some(WebsterPartOfSpeech.VERB)
      else None,
      if (hasNoun) Some(WebsterPartOfSpeech.NOUN) else None,
      if (hasPronoun) Some(WebsterPartOfSpeech.PRONOUN) else None
    ).flatten

    // There are some definitions that will have multiple parts of speech.
    // Our model can't really handle that, and there's no way to automatically tell which one is right
    // So we should throw it out.
    if (matches.size == 1) Some(matches(0)) else None
  }

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
