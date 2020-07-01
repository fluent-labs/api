package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WebsterDefinition(senseSequence: Option[Seq[Seq[WebsterSense]]],
                             verbDivider: Option[String])
// TODO verbDivider seems to be "transitive verb" or "intransitive verb". Opportunity for enum?
object WebsterDefinition {
  def parseSenseSequences(sseq: Option[Seq[Seq[Seq[JsValue]]]],
                          verbDivider: Option[String]): WebsterDefinition = {
    val senseSequence: Option[Seq[Seq[WebsterSense]]] = sseq match {
      case Some(seq) =>
        NestedArrayHelper
        // Gives us a Seq[Map[String, Seq[WebsterSense]]] although we haven't parsed the senses yet
          .buildLookupMapFromNested(seq)
          // We only care about the senses in this array, other types can be safely ignored.
          .flatMap(_.get("sense"))
          // Now it's time to parse the nested objects
          .flatMap(
            sseq =>
              sseq
                .map(sense => sense.validate[WebsterSense])
                // Remove the invalid senses
                .flatMap {
                  case JsSuccess(sense, _) => Some(sense)
                  case JsError(_)          => None
                } match {
                // And then remove the sequence if there are no valid senses in it.
                case Seq() => None
                case s     => Some(s)
            }
          ) match {
          // Final sanity check - Do we have any valid senses? If not then let's be up front about it.
          case Seq() => None
          case sseq  => Some(sseq)
        }
      case None => None
    }
    WebsterDefinition(senseSequence, verbDivider)
  }

  implicit val reads: Reads[WebsterDefinition] = ((JsPath \ "sseq")
    .readNullable[Seq[Seq[Seq[JsValue]]]](
      JsonSequenceHelper.jsValueHelper.readsSeqSeqSeq
    ) and (JsPath \ "vd")
    .readNullable[String])(WebsterDefinition.parseSenseSequences _)
  implicit val writes: Writes[WebsterDefinition] =
    Json.writes[WebsterDefinition]
  implicit val helper: JsonSequenceHelper[WebsterDefinition] =
    new JsonSequenceHelper[WebsterDefinition]
}
