package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import cats.implicits._
import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.sksamuel.elastic4s.ElasticDsl.{indexInto, updateById}
import com.sksamuel.elastic4s.Indexable
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.bulk.BulkCompatibleRequest

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
  * @param lookupId What lookup attempt should we update?
  * @param refetched Did we need to go outside of elasticsearch?
  * @tparam T A case class with Reads[T] and Writes[T] defined.
  */
case class ElasticsearchSearchResult[T: Indexable](index: String,
                                                   fields: Map[String, String],
                                                   result: List[T],
                                                   fetchCount: Int,
                                                   lookupId: Option[String],
                                                   refetched: Boolean,
                                                   queried: Boolean) {
  val attemptsIndex = "attempts"

  val cacheQueries: Option[List[BulkCompatibleRequest]] =
    result match {
      case values if refetched && values.nonEmpty =>
        values.map(v => indexInto(index).doc(v)).some
      case _ => None
    }

  val updateAttemptsQuery: Option[BulkCompatibleRequest] =
    if (refetched) {
      lookupId match {
        case Some(id) =>
          updateById(attemptsIndex, id)
            .doc(
              LookupAttempt(index = index, fields = fields, count = fetchCount)
            )
            .some
        case None =>
          indexInto(attemptsIndex)
            .doc(
              LookupAttempt(index = index, fields = fields, count = fetchCount)
            )
            .some

      }
    } else None

  val toIndex: Option[List[ElasticsearchCacheRequest]] =
    // If we don't know the status of what's in elasticsearch then we should not try to update it.
    // Nothing like failures resetting attempt counts to zero
    if (queried) {
      (cacheQueries, updateAttemptsQuery) match {
        case (Some(cache), Some(attempts)) =>
          ElasticsearchCacheRequest.fromRequests(attempts :: cache).some
        case (None, Some(attempts)) =>
          ElasticsearchCacheRequest.fromRequests(List(attempts)).some
        case _ => None
      }
    } else None
}
