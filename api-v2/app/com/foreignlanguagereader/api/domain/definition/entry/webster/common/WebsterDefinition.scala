package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WebsterDefinition(senseSequence: Option[Seq[Seq[WebsterSense]]],
                             verbDivider: Option[String]) {
  // TODO verbDivider seems to be "transitive verb" or "intransitive verb". Opportunity for enum?
}
object WebsterDefinition {
  // This really ugly hack is because webster uses triple nested lists
  // With both objects and strings in them.
  // This gives us a sane interface to work with
  def convertRawDefinitionToDefinitions(
    sseq: Option[Seq[Seq[Seq[JsValue]]]],
    verbDivider: Option[String]
  ): WebsterDefinition = {
//    val senseSequence: Option[Seq[Seq[WebsterSense]]] = if (sseq.isDefined) {
//      val lookup = NestedArrayHelper.buildLookupMapFromNested(sseq.get)
//      // TODO handle sen, pseq, sdsense, bs
//      // Likely a trait
//      lookup.flatMap(sense => {
//        NestedArrayHelper.getOrNone[WebsterSense](sense.get("sense"))
//      }) match {
//        case sseq if sseq.isEmpty => None
//        case sseq                 => Some(sseq)
//      }
//    } else {
//      None
//    }
    val senseSequence = None
    WebsterDefinition(senseSequence, verbDivider)
  }

  implicit val reads: Reads[WebsterDefinition] = ((JsPath \ "sseq")
    .readNullable[Seq[Seq[Seq[JsValue]]]](
      JsonSequenceHelper.jsValueHelper.readsSeqSeqSeq
    ) and (JsPath \ "vd")
    .readNullable[String])(convertRawDefinitionToDefinitions _)
//  implicit val writes: Writes[WebsterDefinition] =
//    Json.writes[WebsterDefinition]
//  implicit val helper: JsonSequenceHelper[WebsterDefinition] =
//    new JsonSequenceHelper[WebsterDefinition]
}
