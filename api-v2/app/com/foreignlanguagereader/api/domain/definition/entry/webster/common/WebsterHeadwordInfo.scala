package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterHeadwordInfo(
  headword: String,
  pronunciations: Option[Seq[WebsterPronunciation]]
)
object WebsterHeadwordInfo {
  implicit val writes: Writes[WebsterHeadwordInfo] =
    Json.writes[WebsterHeadwordInfo]
  implicit val reads: Reads[WebsterHeadwordInfo] = ((JsPath \ "hw")
    .read[String] and (JsPath \ "prs").readNullable[Seq[WebsterPronunciation]](
    WebsterPronunciation.helper.readsSeq
  ))(WebsterHeadwordInfo.apply _)
}
