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

  val cacheQueries: Option[List[IndexRequest]] = result match {
    case Some(values) => Some(values.map(v => indexInto(index).doc(v)).toList)
    case None         => None
  }
  val attemptsQuery: Option[IndexRequest] =
    if (refetched)
      Some(
        indexInto(attemptsIndex)
          .doc(
            LookupAttempt(index = index, fields = fields, count = fetchCount)
          )
      )
    else None

  val toIndex: Option[List[IndexRequest]] =
    (cacheQueries, attemptsQuery) match {
      case (Some(cache), Some(attempts)) => Some(attempts :: cache)
      case (None, Some(attempts))        => Some(List(attempts))
      case _                             => None
    }
}
