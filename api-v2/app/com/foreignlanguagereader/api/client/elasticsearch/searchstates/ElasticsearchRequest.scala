package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.sksamuel.elastic4s.ElasticDsl.{boolQuery, matchQuery, multi, search}
import com.sksamuel.elastic4s.requests.searches.MultiSearchRequest
import com.sksamuel.elastic4s.requests.searches.queries.BoolQuery

import scala.concurrent.Future

case class ElasticsearchRequest[T](index: String,
                                   fields: Seq[(String, String)],
                                   fetcher: () => Future[Option[Seq[T]]],
                                   maxFetchAttempts: Int) {
  val query: MultiSearchRequest = {
    val searchQuery: BoolQuery = boolQuery().must(fields.map {
      case (field, value) => matchQuery(field, value)
    })

    // TODO
    val attemptsQuery = boolQuery()

    multi(search(index).query(searchQuery), search(index).query(attemptsQuery))
  }
}
