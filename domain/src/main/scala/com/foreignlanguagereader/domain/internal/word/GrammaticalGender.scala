package com.foreignlanguagereader.domain.internal.word

import play.api.libs.json.{Reads, Writes}

object GrammaticalGender extends Enumeration {
  type GrammaticalGender = Value
  val MALE: Value = Value("Male")
  val FEMALE: Value = Value("Female")
  val NEUTER: Value = Value("Neuter")

  implicit val reads: Reads[GrammaticalGender] =
    Reads.enumNameReads(GrammaticalGender)
  implicit val writes: Writes[GrammaticalGender] = Writes.enumNameWrites
}
