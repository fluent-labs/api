package com.foreignlanguagereader.domain.external.definition.webster.common

import com.foreignlanguagereader.domain.external.definition.webster.common.WebsterSource.WebsterSource
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterMeta(
    id: String,
    uuid: String,
    sort: Option[String],
    language: Option[String],
    source: WebsterSource,
    section: String,
    stems: Seq[String],
    offensive: Boolean
)
object WebsterMeta {
  implicit val writes: Writes[WebsterMeta] = Json.writes[WebsterMeta]
  implicit val reads: Reads[WebsterMeta] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "uuid").read[String] and
      (JsPath \ "sort").readNullable[String] and
      (JsPath \ "lang").readNullable[String] and
      (JsPath \ "src").read[WebsterSource] and
      (JsPath \ "section").read[String] and
      (JsPath \ "stems").read[Seq[String]](Reads.seq[String]) and
      (JsPath \ "offensive").read[Boolean]
  )(WebsterMeta.apply _)
}

object WebsterSource extends Enumeration {
  type WebsterSource = Value

  val LEARNERS: Value = Value("learners")
  val SPANISH: Value = Value("spanish")

  implicit val reads: Reads[WebsterSource] = Reads.enumNameReads(WebsterSource)
  implicit val writes: Writes[WebsterSource] = Writes.enumNameWrites
}
