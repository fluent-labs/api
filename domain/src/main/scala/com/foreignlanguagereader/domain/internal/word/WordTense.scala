package com.foreignlanguagereader.domain.internal.word

import play.api.libs.json.{Reads, Writes}

object WordTense extends Enumeration {
  type WordTense = Value
  val CONDITIONAL: Value = Value("Conditional")
  val PAST: Value = Value("Past")
  val PRESENT: Value = Value("Present")
  val FUTURE: Value = Value("Future")
  val IMPERFECT: Value = Value("Imperfect")
  val PLUPERFECT: Value = Value("Pluperfect")

  implicit val reads: Reads[WordTense] = Reads.enumNameReads(WordTense)
  implicit val writes: Writes[WordTense] = Writes.enumNameWrites
}
