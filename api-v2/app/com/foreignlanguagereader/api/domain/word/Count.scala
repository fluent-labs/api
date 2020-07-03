package com.foreignlanguagereader.api.domain.word

import com.foreignlanguagereader.api.domain.word
import play.api.libs.json.{Reads, Writes}

object Count extends Enumeration {
  type Count = Value
  val SINGLE: Value = Value("Single")
  val PLURAL: Value = Value("Plural")
  val DUAL: Value = Value("Dual")

  implicit val reads: Reads[word.WordTense.Value] =
    Reads.enumNameReads(WordTense)
  implicit val writes = Writes.enumNameWrites
}
