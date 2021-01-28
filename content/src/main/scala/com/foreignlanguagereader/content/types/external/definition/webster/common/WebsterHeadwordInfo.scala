package com.foreignlanguagereader.content.types.external.definition.webster.common

import com.foreignlanguagereader.content.formatters.WebsterFormatter
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterHeadwordInfo(
    headword: String,
    pronunciations: Option[List[WebsterPronunciation]],
    alternatePronunciations: Option[List[WebsterPronunciation]]
)
object WebsterHeadwordInfo {
  implicit val writes: Writes[WebsterHeadwordInfo] =
    Json.writes[WebsterHeadwordInfo]
  implicit val reads: Reads[WebsterHeadwordInfo] = (
    (JsPath \ "hw").read[String] and
      (JsPath \ "prs").readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      ) and
      (JsPath \ "altprs").readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      )
  )((hw, prs, altprs) =>
    WebsterHeadwordInfo.apply(WebsterFormatter.format(hw), prs, altprs)
  )
}
