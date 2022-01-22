package io.fluentlabs.domain.client.elasticsearch.searchstates

import cats.syntax.all._
import io.fluentlabs.content.types.internal.ElasticsearchCacheable
import io.fluentlabs.domain.client.elasticsearch.LookupAttempt
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.xcontent.XContentType
import play.api.libs.json.{Json, Writes}

/** Holds the final result from an elasticsearch lookup, Decides whether we need
  * to cache data back to elasticsearch, and also updates the search count.
  *
  * @param index
  *   The elasticsearch index to cache the data. Should just be the type
  * @param fields
  *   The fields needed to look up the correct item. Think of this as the
  *   primary key.
  * @param result
  *   The final result of the query
  * @param fetchCount
  *   How many times has fetch been called?
  * @param lookupId
  *   What lookup attempt should we update?
  * @param refetched
  *   Did we need to go outside of elasticsearch?
  * @tparam T
  *   A case class with Reads[T] and Writes[T] defined.
  */
case class ElasticsearchSearchResult[T: Writes](
    index: String,
    fields: Map[String, String],
    result: List[T],
    fetchCount: Int,
    lookupId: Option[String],
    refetched: Boolean,
    queried: Boolean
) {
  val attemptsIndex = "attempts"

  val cacheQueries: Option[List[Either[IndexRequest, UpdateRequest]]] =
    result match {
      case values if refetched && values.nonEmpty =>
        values
          .map(v => {
            val cacheable: ElasticsearchCacheable[T] =
              ElasticsearchCacheable(v, fields)
            new IndexRequest()
              .source(Json.toJson(cacheable).toString(), XContentType.JSON)
              .index(index)
              .asLeft
          })
          .some
      case _ => None
    }

  val updateAttemptsQuery: Option[Either[IndexRequest, UpdateRequest]] =
    if (refetched) {
      val attempt =
        LookupAttempt(index = index, fields = fields, count = fetchCount)

      val indexRequest = new IndexRequest()
        .source(Json.toJson(attempt).toString(), XContentType.JSON)
        .index(attemptsIndex)

      lookupId match {
        case Some(id) =>
          new UpdateRequest(attemptsIndex, id)
            .upsert(indexRequest.id(id))
            .doc(indexRequest.id(id))
            .asRight
            .some
        case None =>
          new IndexRequest()
            .source(Json.toJson(attempt).toString(), XContentType.JSON)
            .index(attemptsIndex)
            .asLeft
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
