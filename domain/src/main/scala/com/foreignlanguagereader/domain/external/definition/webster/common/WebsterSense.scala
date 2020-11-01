package com.foreignlanguagereader.domain.external.definition.webster.common

import com.foreignlanguagereader.domain.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WebsterSense(
    definingText: WebsterDefiningText,
    inflections: Option[List[WebsterInflection]],
    labels: Option[List[String]],
    pronunciations: Option[List[WebsterPronunciation]],
    dividedSense: Option[WebsterDividedSense],
    senseSpecificGrammaticalLabel: Option[String],
    subjectStatusLabels: Option[List[String]],
    variations: Option[List[WebsterVariant]]
)
object WebsterSense {
  implicit val readsString: Reads[List[String]] = Reads.list[String]
  implicit val reads: Reads[WebsterSense] = (
    (JsPath \ "dt").read[WebsterDefiningText] and (JsPath \ "ins")
      .readNullable[List[WebsterInflection]](
        WebsterInflection.helper.readsList
      ) and (JsPath \ "lbs")
      .readNullable[List[String]] and (JsPath \ "prs")
      .readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      ) and (JsPath \ "sdsense")
      .readNullable[WebsterDividedSense](
        WebsterDividedSense.reads
      ) and (JsPath \ "sgram")
      .readNullable[String] and (JsPath \ "sls")
      .readNullable[List[String]] and (JsPath \ "vrs")
      .readNullable[List[WebsterVariant]](WebsterVariant.helper.readsList)
  )(WebsterSense.apply _)
  implicit val writes: Writes[WebsterSense] = Json.writes[WebsterSense]
  implicit val helper: JsonSequenceHelper[WebsterSense] =
    new JsonSequenceHelper[WebsterSense]
}

case class WebsterDividedSense(
    senseDivider: String,
    definingText: WebsterDefiningText,
    inflections: Option[List[WebsterInflection]],
    labels: Option[List[String]],
    pronunciations: Option[List[WebsterPronunciation]],
    senseSpecificGrammaticalLabel: Option[String],
    subjectStatusLabels: Option[List[String]],
    variations: Option[List[WebsterVariant]]
)
object WebsterDividedSense {
  implicit val readsString: Reads[List[String]] = Reads.list[String]
  implicit val reads: Reads[WebsterDividedSense] = (
    (JsPath \ "sd").read[String] and (JsPath \ "dt")
      .read[WebsterDefiningText] and (JsPath \ "ins")
      .readNullable[List[WebsterInflection]](
        WebsterInflection.helper.readsList
      ) and (JsPath \ "lbs")
      .readNullable[List[String]] and (JsPath \ "prs")
      .readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      ) and (JsPath \ "sgram")
      .readNullable[String] and (JsPath \ "sls")
      .readNullable[List[String]] and (JsPath \ "vrs")
      .readNullable[List[WebsterVariant]](WebsterVariant.helper.readsList)
  )(WebsterDividedSense.apply _)
  implicit val writes: Writes[WebsterDividedSense] =
    Json.writes[WebsterDividedSense]
  implicit val helper: JsonSequenceHelper[WebsterDividedSense] =
    new JsonSequenceHelper[WebsterDividedSense]
}
