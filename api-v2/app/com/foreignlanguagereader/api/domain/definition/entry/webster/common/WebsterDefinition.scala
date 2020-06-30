package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WebsterDefinition(senseSequence: Option[Seq[Seq[WebsterSense]]],
                             verbDivider: Option[String])
// TODO verbDivider seems to be "transitive verb" or "intransitive verb". Opportunity for enum?
object WebsterDefinition {
  implicit val reads: Reads[WebsterDefinition] = ((JsPath \ "sseq")
    .readNullable[Seq[Seq[WebsterSense]]](WebsterSense.helper.readsSeqSeq) and (JsPath \ "vd")
    .readNullable[String])(WebsterDefinition.apply _)
  implicit val writes: Writes[WebsterDefinition] =
    Json.writes[WebsterDefinition]
  implicit val helper: JsonSequenceHelper[WebsterDefinition] =
    new JsonSequenceHelper[WebsterDefinition]
}
