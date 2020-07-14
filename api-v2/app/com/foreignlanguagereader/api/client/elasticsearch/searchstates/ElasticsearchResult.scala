package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import com.sksamuel.elastic4s.Indexable
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.indexes.IndexRequest

/**
  *
  * Holds the final result from an elasticsearch lookup,
  * Decides whether we need to cache data back to elasticsearch,
  * and also updates the search count.
  *
  * @param index The elasticsearch index to cache the data. Should just be the type
  * @param fields The fields needed to look up the correct item. Think of this as the primary key.
  * @param result The final result of the query
  * @param fetchCount How many times has fetch been called?
  * @param refetched Did we refetch this data?
  * @tparam T A case class with Reads[T] and Writes[T] defined.
  */
case class ElasticsearchResult[T: Indexable](index: String,
                                             fields: Map[String, String],
                                             result: Option[Seq[T]],
                                             fetchCount: Int,
                                             refetched: Boolean) {
  val attemptsIndex = "attempts"

  val cacheQueries: List[IndexRequest] = result match {
    case Some(values) => values.map(v => indexInto(index).doc(v)).toList
    case None         => List()
  }
  val attemptsQuery: IndexRequest = indexInto(attemptsIndex)
    .doc(
      LookupAttempt(index = index, fields = fields.toMap, count = fetchCount)
    )

  val toIndex: Option[List[IndexRequest]] =
    if (refetched) Some(attemptsQuery :: cacheQueries)
    else None
}
