package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import com.sksamuel.elastic4s.Indexable
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.indexes.IndexRequest

case class ElasticsearchResult[T: Indexable](index: String,
                                             fields: Seq[(String, String)],
                                             result: Option[Seq[T]],
                                             fetchCount: Int,
                                             refetched: Boolean) {
  val attemptsIndex = "attempts"

  val cacheQueries: List[IndexRequest] = result match {
    case Some(values) => values.map(v => indexInto(index).doc(v)).toList
    case None         => List()
  }
  val attemptsQuery: IndexRequest = indexInto(attemptsIndex)
    .doc(LookupAttempt(index = index, fields = fields, count = fetchCount))

  val toIndex: Option[List[IndexRequest]] =
    if (refetched) Some(attemptsQuery :: cacheQueries)
    else None
}
