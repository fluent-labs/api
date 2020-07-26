package com.foreignlanguagereader.api.contentsource.definition.webster.common

import com.foreignlanguagereader.api.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterDefinedRunOnPhrase(
  definedRunOnPhrase: String,
  definition: Seq[WebsterDefinition],
  labels: Option[Seq[String]],
  pronunciations: Option[Seq[WebsterPronunciation]],
  areaOfUsage: Option[String],
  subjectStatusLabels: Option[Seq[String]],
  variations: Option[Seq[WebsterVariant]]
)
object WebsterDefinedRunOnPhrase {
  implicit val readsString: Reads[Seq[String]] = Reads.seq[String]
  implicit val reads: Reads[WebsterDefinedRunOnPhrase] = (
    (JsPath \ "drp").read[String] and (JsPath \ "def")
      .read[Seq[WebsterDefinition]](WebsterDefinition.helper.readsSeq) and (JsPath \ "lbs")
      .readNullable[Seq[String]] and (JsPath \ "prs")
      .readNullable[Seq[WebsterPronunciation]](
        WebsterPronunciation.helper.readsSeq
      ) and (JsPath \ "psl")
      .readNullable[String] and (JsPath \ "sls")
      .readNullable[Seq[String]] and (JsPath \ "vrs")
      .readNullable[Seq[WebsterVariant]](WebsterVariant.helper.readsSeq)
  )(WebsterDefinedRunOnPhrase.apply _)
  implicit val writes: Writes[WebsterDefinedRunOnPhrase] =
    Json.writes[WebsterDefinedRunOnPhrase]
  implicit val helper: JsonSequenceHelper[WebsterDefinedRunOnPhrase] =
    new JsonSequenceHelper[WebsterDefinedRunOnPhrase]
}
