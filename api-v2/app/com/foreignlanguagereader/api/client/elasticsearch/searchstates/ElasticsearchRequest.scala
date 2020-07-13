package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.sksamuel.elastic4s.ElasticDsl.{boolQuery, matchQuery, multi, search}
import com.sksamuel.elastic4s.requests.searches.MultiSearchRequest
import com.sksamuel.elastic4s.requests.searches.queries.BoolQuery
import com.sksamuel.elastic4s.requests.searches.queries.matches.MatchQuery

import scala.concurrent.Future

case class ElasticsearchRequest[T](index: String,
                                   fields: Seq[(String, String)],
                                   fetcher: () => Future[Option[Seq[T]]],
                                   maxFetchAttempts: Int) {
  val attemptsIndex = "attempts"

  val query: MultiSearchRequest = {
    val searchQuery: BoolQuery = boolQuery().must(fields.map {
      case (field, value) => matchQuery(field, value)
    })

    // This finds out if we've searched for this before
    // It'll be used later to know if we should try to fetch if there are no elasticsearch results
    val attemptsQuery = {
      val queries: Seq[MatchQuery] = fields.map {
        case (field, value) => matchQuery(s"field.$field", value)
      }
      boolQuery().must(matchQuery("index", index) +: queries)
    }

    multi(
      search(index).query(searchQuery),
      search(attemptsIndex).query(attemptsQuery)
    )
  }
}
