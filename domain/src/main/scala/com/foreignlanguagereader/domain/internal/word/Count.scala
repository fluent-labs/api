package com.foreignlanguagereader.domain.internal.word

import play.api.libs.json.{Reads, Writes}

object Count extends Enumeration {
  type Count = Value
  val SINGLE: Value = Value("Single")
  val PLURAL: Value = Value("Plural")
  val DUAL: Value = Value("Dual")

  implicit val reads: Reads[Count] = Reads.enumNameReads(Count)
  implicit val writes: Writes[Count] = Writes.enumNameWrites
}
