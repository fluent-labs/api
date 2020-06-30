package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterVariant(variant: String,
                          variantLabel: Option[String],
                          pronunciations: Option[Seq[WebsterPronunciation]])
object WebsterVariant {
  implicit val writes: Writes[WebsterVariant] = Json.writes[WebsterVariant]
  implicit val reads: Reads[WebsterVariant] = ((JsPath \ "va")
    .read[String] and
    (JsPath \ "vl").readNullable[String] and
    (JsPath \ "prs")
      .readNullable[Seq[WebsterPronunciation]](
        WebsterPronunciation.helper.readsSeq
      ))(WebsterVariant.apply _)
}
