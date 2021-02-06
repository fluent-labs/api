package com.foreignlanguagereader.content.difficulty

import cats.implicits._
import play.api.libs.json.{Reads, Writes}

object CEFRDifficultyLevel extends Enumeration {
  type CEFRDifficultyLevel = Value
  val A1_BEGINNING: Value = Value("A1")
  val A2_ELEMENTARY: Value = Value("A2")
  val B1_INTERMEDIATE: Value = Value("B1")
  val B2_UPPER_INTERMEDIATE: Value = Value("B2")
  val C1_ADVANCED: Value = Value("C1")
  val C2_PROFICIENCY: Value = Value("C2")

  def fromString(s: String): Option[CEFRDifficultyLevel] =
    CEFRDifficultyLevel.values.find(_.toString === s)

  implicit val reads: Reads[CEFRDifficultyLevel] =
    Reads.enumNameReads(CEFRDifficultyLevel)
  implicit val writes: Writes[CEFRDifficultyLevel] = Writes.enumNameWrites
}
