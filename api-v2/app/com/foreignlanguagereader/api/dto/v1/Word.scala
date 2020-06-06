package com.foreignlanguagereader.api.dto.v1

import play.api.libs.json.{Format, Json}

case class Word(token: String, tag: String, lemma: String)

object Word {
  implicit val format: Format[Word] = Json.format
}
