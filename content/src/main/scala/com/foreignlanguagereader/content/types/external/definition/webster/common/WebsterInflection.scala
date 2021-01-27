package com.foreignlanguagereader.content.types.external.definition.webster.common

import com.foreignlanguagereader.content.formatters.WebsterFormatter
import com.foreignlanguagereader.content.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterInflection(
    inflection: Option[String],
    inflectionCutback: Option[String],
    inflectionLabel: Option[String],
    pronunciation: Option[WebsterPronunciation]
)
object WebsterInflection {
  implicit val writes: Writes[WebsterInflection] =
    Json.writes[WebsterInflection]
  implicit val reads: Reads[WebsterInflection] = (
    (JsPath \ "if").readNullable[String] and
      (JsPath \ "ifc").readNullable[String] and
      (JsPath \ "il").readNullable[String] and
      (JsPath \ "prs").readNullable[WebsterPronunciation]
  )((iff, ifc, il, prs) =>
    WebsterInflection.apply(
      iff.map(WebsterFormatter.format),
      ifc.map(WebsterFormatter.format),
      il.map(WebsterFormatter.format),
      prs
    )
  )
  implicit val helper: JsonSequenceHelper[WebsterInflection] =
    new JsonSequenceHelper[WebsterInflection]
}
