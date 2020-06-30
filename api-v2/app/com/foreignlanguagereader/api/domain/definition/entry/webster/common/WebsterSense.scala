package com.foreignlanguagereader.api.domain.definition.entry.webster.common

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WebsterSense(definingText: WebsterDefiningText,
                        inflections: Option[Seq[WebsterInflection]],
                        labels: Option[Seq[String]],
                        pronunciations: Option[Seq[WebsterPronunciation]],
                        dividedSense: Option[WebsterDividedSense],
                        senseSpecificGrammaticalLabel: Option[String],
                        subjectStatusLabels: Option[Seq[String]],
                        variations: Option[Seq[WebsterVariant]])
object WebsterSense {
  implicit val readsString: Reads[Seq[String]] = Reads.seq[String]
  implicit val reads: Reads[WebsterSense] = (
    (JsPath \ "dt").read[WebsterDefiningText] and (JsPath \ "ins")
      .readNullable[Seq[WebsterInflection]](WebsterInflection.helper.readsSeq) and (JsPath \ "lbs")
      .readNullable[Seq[String]] and (JsPath \ "prs")
      .readNullable[Seq[WebsterPronunciation]](
        WebsterPronunciation.helper.readsSeq
      ) and (JsPath \ "sdsense")
      .readNullable[WebsterDividedSense](WebsterDividedSense.reads) and (JsPath \ "sgram")
      .readNullable[String] and (JsPath \ "sls")
      .readNullable[Seq[String]] and (JsPath \ "vrs")
      .readNullable[Seq[WebsterVariant]](WebsterVariant.helper.readsSeq)
  )(WebsterSense.apply _)
  implicit val writes: Writes[WebsterSense] = Json.writes[WebsterSense]
  implicit val helper: JsonSequenceHelper[WebsterSense] =
    new JsonSequenceHelper[WebsterSense]
}

case class WebsterDividedSense(
  senseDivider: String,
  definingText: WebsterDefiningText,
  inflections: Option[Seq[WebsterInflection]],
  labels: Option[Seq[String]],
  pronunciations: Option[Seq[WebsterPronunciation]],
  senseSpecificGrammaticalLabel: Option[String],
  subjectStatusLabels: Option[Seq[String]],
  variations: Option[Seq[WebsterVariant]]
)
object WebsterDividedSense {
  implicit val readsString: Reads[Seq[String]] = Reads.seq[String]
  implicit val reads: Reads[WebsterDividedSense] = (
    (JsPath \ "sd").read[String] and (JsPath \ "dt")
      .read[WebsterDefiningText] and (JsPath \ "ins")
      .readNullable[Seq[WebsterInflection]](WebsterInflection.helper.readsSeq) and (JsPath \ "lbs")
      .readNullable[Seq[String]] and (JsPath \ "prs")
      .readNullable[Seq[WebsterPronunciation]](
        WebsterPronunciation.helper.readsSeq
      ) and (JsPath \ "sgram")
      .readNullable[String] and (JsPath \ "sls")
      .readNullable[Seq[String]] and (JsPath \ "vrs")
      .readNullable[Seq[WebsterVariant]](WebsterVariant.helper.readsSeq)
  )(WebsterDividedSense.apply _)
  implicit val writes: Writes[WebsterDividedSense] =
    Json.writes[WebsterDividedSense]
  implicit val helper: JsonSequenceHelper[WebsterDividedSense] =
    new JsonSequenceHelper[WebsterDividedSense]
}
