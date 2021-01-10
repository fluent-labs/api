package com.foreignlanguagereader.content.types.internal.word

import cats.implicits._
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

  def fromString(s: String): Option[WordTense] =
    WordTense.values.find(_.toString === s)
}
