package com.foreignlanguagereader.content.types.internal.word

import cats.syntax.all._
import play.api.libs.json.{Reads, Writes}

object GrammaticalGender extends Enumeration {
  type GrammaticalGender = Value
  val MALE: Value = Value("Male")
  val FEMALE: Value = Value("Female")
  val NEUTER: Value = Value("Neuter")

  implicit val reads: Reads[GrammaticalGender] =
    Reads.enumNameReads(GrammaticalGender)
  implicit val writes: Writes[GrammaticalGender] = Writes.enumNameWrites

  def fromString(s: String): Option[GrammaticalGender] =
    GrammaticalGender.values.find(_.toString === s)
}
