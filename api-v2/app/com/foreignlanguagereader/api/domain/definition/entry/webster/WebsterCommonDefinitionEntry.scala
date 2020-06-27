package com.foreignlanguagereader.api.domain.definition.entry.webster

import play.api.libs.json._
import play.api.libs.json.Reads._

case class WebsterMeta(id: String,
                       uuid: String,
                       sort: String,
                       src: String,
                       section: String,
                       stems: Seq[String],
                       offensive: Boolean)
object WebsterMeta {
  implicit val format: Format[WebsterMeta] = Json.format[WebsterMeta]
}
