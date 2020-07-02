package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterInflection(inflection: Option[String],
                             inflectionCutback: Option[String],
                             inflectionLabel: Option[String],
                             pronunciation: Option[WebsterPronunciation])
object WebsterInflection {
  implicit val writes: Writes[WebsterInflection] =
    Json.writes[WebsterInflection]
  implicit val reads: Reads[WebsterInflection] = (
    (JsPath \ "if").readNullable[String] and
      (JsPath \ "ifc").readNullable[String] and
      (JsPath \ "il").readNullable[String] and
      (JsPath \ "prs").readNullable[WebsterPronunciation]
  )(WebsterInflection.apply _)
  implicit val helper: JsonSequenceHelper[WebsterInflection] =
    new JsonSequenceHelper[WebsterInflection]
}
