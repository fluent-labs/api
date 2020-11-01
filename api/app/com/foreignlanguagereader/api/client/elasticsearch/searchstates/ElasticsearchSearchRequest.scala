package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.common.CircuitBreakerResult
import com.sksamuel.elastic4s.ElasticDsl.{boolQuery, matchQuery, multi, search}
import com.sksamuel.elastic4s.requests.searches.MultiSearchRequest
import com.sksamuel.elastic4s.requests.searches.queries.BoolQuery
import com.sksamuel.elastic4s.requests.searches.queries.matches.MatchQuery

import scala.concurrent.Future

/**
  * Takes the absolute minimum information from the caller
  * and generates the correct elasticsearch queries to cache this type.
  *
  * @param index The elasticsearch index to cache the data. Should just be the type
  * @param fields The fields needed to look up the correct item. Think of this as the primary key.
  * @param fetcher A function to be called if results are not in elasticsearch, which can try to get the results again.
  * @param maxFetchAttempts If we don't have any results, how many times should we search for this? Highly source dependent.
  * @tparam T A case class with Reads[T] and Writes[T] defined.
  */
case class ElasticsearchSearchRequest[T](
  index: String,
  fields: Map[String, String],
  fetcher: () => Future[CircuitBreakerResult[List[T]]],
  maxFetchAttempts: Int
) {
  val attemptsIndex = "attempts"

  // This finds out if we've searched for this before
  // It'll be used later to know if we should try to fetch if there are no elasticsearch results
  val attemptsQuery: BoolQuery = {
    val queries: Seq[MatchQuery] = fields.map {
      case (field, value) => matchQuery(s"fields.$field", value)
    }.toSeq
    boolQuery().must(matchQuery("index", index) +: queries)
  }

  val searchQuery: BoolQuery = boolQuery().must(fields.map {
    case (field, value) => matchQuery(field, value)
  })

  val query: MultiSearchRequest =
    multi(
      search(index).query(searchQuery),
      search(attemptsIndex).query(attemptsQuery)
    )
}
