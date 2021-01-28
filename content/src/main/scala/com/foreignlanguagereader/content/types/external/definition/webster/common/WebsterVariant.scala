package com.foreignlanguagereader.content.types.external.definition.webster.common

import com.foreignlanguagereader.content.formatters.WebsterFormatter
import com.foreignlanguagereader.content.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterVariant(
    variant: String,
    variantLabel: Option[String],
    pronunciations: Option[List[WebsterPronunciation]]
)
object WebsterVariant {
  implicit val reads: Reads[WebsterVariant] = ((JsPath \ "va")
    .read[String] and
    (JsPath \ "vl").readNullable[String] and
    (JsPath \ "prs")
      .readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      ))((va, vl, prs) =>
    WebsterVariant
      .apply(
        WebsterFormatter.format(va),
        WebsterFormatter.formatOptional(vl),
        prs
      )
  )
  implicit val writes: Writes[WebsterVariant] = Json.writes[WebsterVariant]
  implicit val helper: JsonSequenceHelper[WebsterVariant] =
    new JsonSequenceHelper[WebsterVariant]
}
