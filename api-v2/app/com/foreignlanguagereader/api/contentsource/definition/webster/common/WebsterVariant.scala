package com.foreignlanguagereader.api.contentsource.definition.webster.common

import com.foreignlanguagereader.api.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterVariant(variant: String,
                          variantLabel: Option[String],
                          pronunciations: Option[Seq[WebsterPronunciation]])
object WebsterVariant {
  implicit val reads: Reads[WebsterVariant] = ((JsPath \ "va")
    .read[String] and
    (JsPath \ "vl").readNullable[String] and
    (JsPath \ "prs")
      .readNullable[Seq[WebsterPronunciation]](
        WebsterPronunciation.helper.readsSeq
      ))(WebsterVariant.apply _)
  implicit val writes: Writes[WebsterVariant] = Json.writes[WebsterVariant]
  implicit val helper: JsonSequenceHelper[WebsterVariant] =
    new JsonSequenceHelper[WebsterVariant]
}
