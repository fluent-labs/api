package com.foreignlanguagereader.api.util

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
