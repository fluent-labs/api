package com.foreignlanguagereader.domain.external.definition.webster.common

import com.foreignlanguagereader.domain.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WebsterDefiningText(
    text: Seq[String],
    biographicalName: Option[Seq[WebsterBiographicalNameWrap]],
    calledAlso: Option[Seq[WebsterCalledAlso]],
    supplementalNote: Option[Seq[WebsterSupplementalNote]],
    examples: Option[Seq[WebsterVerbalIllustration]]
)
object WebsterDefiningText {

  /**
    * This class uses a data type of a series of nested arrays
    * See NestedArrayHelper for details
    */
  implicit val reads: Reads[WebsterDefiningText] = JsPath
    .read[List[List[JsValue]]](JsonSequenceHelper.jsValueHelper.readsListList)
    .map(intermediate => {
      val lookup: Map[String, List[JsValue]] =
        WebsterNestedArrayHelper.buildLookupMap(intermediate)

      val text: List[String] =
        WebsterNestedArrayHelper.getOrNone[String](lookup.get("text")) match {
          case Some(t) => t
          case None    => List()
        }
      val bnw: Option[List[WebsterBiographicalNameWrap]] =
        WebsterNestedArrayHelper
          .getOrNone[WebsterBiographicalNameWrap](lookup.get("bnw"))
      val ca: Option[List[WebsterCalledAlso]] =
        WebsterNestedArrayHelper.getOrNone[WebsterCalledAlso](lookup.get("ca"))

      val snote: Option[List[WebsterSupplementalNote]] =
        WebsterNestedArrayHelper
          .getOrNone[WebsterSupplementalNote](lookup.get("snote"))

      val vis: Option[List[WebsterVerbalIllustration]] =
        WebsterNestedArrayHelper
          .getOrNone[List[WebsterVerbalIllustration]](lookup.get("vis"))(
            WebsterVerbalIllustration.helper.readsList
          ) match {
          case Some(x) => Some(x.flatten)
          case None    => None
        }

      WebsterDefiningText(text, bnw, ca, snote, vis)
    })
    .filter(JsonValidationError("Text is a required field"))(d =>
      d.text.nonEmpty
    )

  implicit val writes: Writes[WebsterDefiningText] =
    Json.writes[WebsterDefiningText]
}

case class WebsterBiographicalNameWrap(
    personalName: Option[String],
    surname: Option[String],
    alternateName: Option[String],
    pronunciations: Option[Seq[WebsterPronunciation]]
)
object WebsterBiographicalNameWrap {
  implicit val reads: Reads[WebsterBiographicalNameWrap] = (
    (JsPath \ "pname").readNullable[String] and
      (JsPath \ "sname").readNullable[String] and
      (JsPath \ "altname").readNullable[String] and
      (JsPath \ "prs").readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      )
  )(WebsterBiographicalNameWrap.apply _)
  implicit val writes: Writes[WebsterBiographicalNameWrap] =
    Json.writes[WebsterBiographicalNameWrap]
  implicit val helper: JsonSequenceHelper[WebsterBiographicalNameWrap] =
    new JsonSequenceHelper[WebsterBiographicalNameWrap]
}

case class WebsterCalledAlso(
    intro: Option[String],
    calledAlsoTargets: Option[Seq[WebsterCalledAlsoTarget]]
)
object WebsterCalledAlso {
  implicit val reads: Reads[WebsterCalledAlso] =
    ((JsPath \ "intro").readNullable[String] and
      (JsPath \ "cats")
        .readNullable[List[WebsterCalledAlsoTarget]](
          WebsterCalledAlsoTarget.helper.readsList
        ))(WebsterCalledAlso.apply _)
  implicit val writes: Writes[WebsterCalledAlso] =
    Json.writes[WebsterCalledAlso]
}

case class WebsterCalledAlsoTarget(
    calledAlsoTargetText: Option[String],
    calledAlsoReference: Option[String],
    parenthesizedNumber: Option[String],
    pronunciations: Option[Seq[WebsterPronunciation]],
    areaOfUsage: Option[String]
)
object WebsterCalledAlsoTarget {
  implicit val reads: Reads[WebsterCalledAlsoTarget] = (
    (JsPath \ "cat").readNullable[String] and
      (JsPath \ "catref").readNullable[String] and
      (JsPath \ "pn").readNullable[String] and
      (JsPath \ "prs").readNullable[List[WebsterPronunciation]](
        WebsterPronunciation.helper.readsList
      ) and
      (JsPath \ "psl").readNullable[String]
  )(WebsterCalledAlsoTarget.apply _)
  implicit val writes: Writes[WebsterCalledAlsoTarget] =
    Json.writes[WebsterCalledAlsoTarget]
  implicit val helper: JsonSequenceHelper[WebsterCalledAlsoTarget] =
    new JsonSequenceHelper[WebsterCalledAlsoTarget]
}

case class WebsterSupplementalNote(
    text: String,
    example: Option[Seq[WebsterVerbalIllustration]]
)
// Note: This class can also contain run ins (ri). It's not really useful for this application so I have dropped it.
object WebsterSupplementalNote {
  implicit val reads: Reads[WebsterSupplementalNote] = JsPath
    .read[List[List[JsValue]]](JsonSequenceHelper.jsValueHelper.readsListList)
    .map(intermediate => {
      val lookup = WebsterNestedArrayHelper.buildLookupMap(intermediate)

      val text =
        WebsterNestedArrayHelper.getOrNone[String](lookup.get("t")) match {
          case Some(t) => t
          case None    => List("")
        }

      val example: Option[List[WebsterVerbalIllustration]] = {
        val nested = WebsterNestedArrayHelper
          .getOrNone[List[WebsterVerbalIllustration]](lookup.get("vis"))(
            WebsterVerbalIllustration.helper.readsList
          )
        nested match {
          case Some(x) => Some(x.flatten)
          case None    => None
        }
      }

      WebsterSupplementalNote(text(0), example)
    })
    .filter(JsonValidationError("Text is a required field"))(d =>
      d.text.nonEmpty
    )

  implicit val writes: Writes[WebsterSupplementalNote] =
    Json.writes[WebsterSupplementalNote]
  implicit val helper: JsonSequenceHelper[WebsterSupplementalNote] =
    new JsonSequenceHelper[WebsterSupplementalNote]
}

case class WebsterVerbalIllustration(text: String)
object WebsterVerbalIllustration {
  implicit val reads: Reads[WebsterVerbalIllustration] =
    (JsPath \ "t").read[String].map(t => WebsterVerbalIllustration(t))
  implicit val writes: Writes[WebsterVerbalIllustration] =
    Json.writes[WebsterVerbalIllustration]
  implicit val helper: JsonSequenceHelper[WebsterVerbalIllustration] =
    new JsonSequenceHelper[WebsterVerbalIllustration]
}
