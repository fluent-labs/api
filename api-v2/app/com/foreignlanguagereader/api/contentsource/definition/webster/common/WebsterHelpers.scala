package com.foreignlanguagereader.api.contentsource.definition.webster.common

import play.api.libs.json._

/**
  * This helper class exists to get rid of all the boilerplate with nested json parsers
  * The macros don't handle nested types without being told that they can.
  * So this tells them that they can
  *
  * @param reads An implementation of json reading. Just needs to be in scope.
  * @param writes An implementation of json writing. Just needs to be in scope.
  * @tparam T The type we are helping with.
  */
class JsonSequenceHelper[T](implicit val reads: Reads[T],
                            implicit val writes: Writes[T]) {
  implicit val readsSeq: Reads[Seq[T]] = Reads.seq(reads)
  implicit val readsSeqSeq: Reads[Seq[Seq[T]]] = Reads.seq(readsSeq)
  implicit val readsSeqSeqSeq: Reads[Seq[Seq[Seq[T]]]] = Reads.seq(readsSeqSeq)
  implicit val writesSeq: Writes[Seq[T]] = Writes.seq(writes)
  implicit val writesSeqSeq: Writes[Seq[Seq[T]]] = Writes.seq(writesSeq)
  implicit val writesSeqSeqSeq: Writes[Seq[Seq[Seq[T]]]] =
    Writes.seq(writesSeqSeq)
}
object JsonSequenceHelper {
  val jsValueHelper = new JsonSequenceHelper[JsValue]
}

/**
  * Webster has a data structure that uses nested lists.
  * ["key", [[ something ] ... [something]]
  * Those don't fit very well in our JSON model, so this class is here to help with parsing it.
  *
  * Build a lookup map, and then classes can pull them out.
  */
object NestedArrayHelper {
  def buildLookupMap(input: Seq[Seq[JsValue]]): Map[String, Seq[JsValue]] =
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
    input: Seq[Seq[Seq[JsValue]]]
  ): Seq[Map[String, Seq[JsValue]]] = input.map(buildLookupMap)

  // Helper method to handle pulling optional sequences out of the json.
  def getOrNone[T](
    input: Option[Seq[JsValue]]
  )(implicit r: Reads[T]): Option[Seq[T]] = {
    implicit val readsSeq: Reads[Seq[T]] = Reads.seq(r)
    input match {
      case Some(x) =>
        val validated = x.flatMap(
          v =>
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
