package com.foreignlanguagereader.api.domain.word

import com.foreignlanguagereader.api.domain.word
import play.api.libs.json.{Reads, Writes}

object GrammaticalGender extends Enumeration {
  type GrammaticalGender = Value
  val MALE: Value = Value("Male")
  val FEMALE: Value = Value("Female")
  val NEUTER: Value = Value("Neuter")

  implicit val reads: Reads[word.GrammaticalGender.Value] =
    Reads.enumNameReads(GrammaticalGender)
  implicit val writes = Writes.enumNameWrites
}
