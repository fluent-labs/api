package com.foreignlanguagereader.api.dto.v1.document

import play.api.libs.json.{Format, Json, Reads}

case class Word(token: String, tag: String, lemma: String)

object Word {
  implicit val format: Format[Word] = Json.format
  implicit val reads: Reads[Word] =
    Json.reads[Word]
  implicit val readsSeq: Reads[Seq[Word]] =
    Reads.seq(reads)
}
