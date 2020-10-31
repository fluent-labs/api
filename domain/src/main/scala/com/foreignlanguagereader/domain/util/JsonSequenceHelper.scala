package com.foreignlanguagereader.domain.util

import play.api.libs.json.{JsValue, Reads, Writes}

/**
  * This helper class exists to get rid of all the boilerplate with nested json parsers
  * The macros don't handle nested types without being told that they can.
  * So this tells them that they can
  *
  * @param reads An implementation of json reading. Just needs to be in scope.
  * @param writes An implementation of json writing. Just needs to be in scope.
  * @tparam T The type we are helping with.
  */
class JsonSequenceHelper[T](
    implicit val reads: Reads[T],
    implicit val writes: Writes[T]
) {
  implicit val readsList: Reads[List[T]] = Reads.list(reads)
  implicit val readsListList: Reads[List[List[T]]] = Reads.list(readsList)
  implicit val readsListListList: Reads[List[List[List[T]]]] =
    Reads.list(readsListList)
  implicit val writesList: Writes[List[T]] = Writes.list(writes)
  implicit val writesListList: Writes[List[List[T]]] = Writes.list(writesList)
  implicit val writesListListList: Writes[List[List[List[T]]]] =
    Writes.list(writesListList)
}
object JsonSequenceHelper {
  val jsValueHelper = new JsonSequenceHelper[JsValue]
}
