package com.foreignlanguagereader.domain.external.definition.webster.common

import play.api.libs.json._

/*
 * Webster has a data structure that uses nested lists.
 * ["key", [[ something ] ... [something]]
 * Those don't fit very well in our JSON model, so this class is here to help with parsing it.
 *
 * Build a lookup map, and then classes can pull them out.
 */
object WebsterNestedArrayHelper {
  def buildLookupMap(input: List[List[JsValue]]): Map[String, List[JsValue]] =
    input
      .map(row => {
        val typeString = row(0).validate[String] match {
          case JsSuccess(t, _) => t
          case JsError(e)      => throw new IllegalArgumentException(e.toString)
        }
        typeString -> row(1)
      })
      .groupBy { case (key, _) => key }
      .map {
        case (key, values) => key -> values.map { case (_, value) => value }
      }

  def buildLookupMapFromNested(
      input: List[List[List[JsValue]]]
  ): List[Map[String, List[JsValue]]] = input.map(buildLookupMap)

  // Helper method to handle pulling optional sequences out of the json.
  def getOrNone[T](
      input: Option[List[JsValue]]
  )(implicit r: Reads[T]): Option[List[T]] = {
    implicit val readsList: Reads[List[T]] = Reads.list(r)
    input match {
      case Some(x) =>
        val validated = x.flatMap(v =>
          v.validate[T] match {
            case JsSuccess(value, _) => Some(value)
            case JsError(_)          => None
          }
        )
        if (validated.nonEmpty) Some(validated) else None
      case None => None
    }
  }
}
