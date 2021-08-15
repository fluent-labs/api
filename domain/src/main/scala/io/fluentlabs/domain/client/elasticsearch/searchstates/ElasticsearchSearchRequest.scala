package io.fluentlabs.domain.client.elasticsearch.searchstates

import io.fluentlabs.domain.client.circuitbreaker.CircuitBreakerResult
import org.elasticsearch.action.search.{MultiSearchRequest, SearchRequest}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.concurrent.Future

/** Takes the absolute minimum information from the caller
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
  val attemptsQuery: SearchRequest = {
    val q = fields
      .foldLeft(QueryBuilders.boolQuery()) { case (acc, (field, value)) =>
        acc.must(QueryBuilders.matchQuery(s"fields.$field", value))
      }
      .must(QueryBuilders.matchQuery("index", index))
    new SearchRequest()
      .source(new SearchSourceBuilder().query(q))
      .indices(attemptsIndex)
  }

  val searchQuery: SearchRequest = {
    val q = fields.foldLeft(QueryBuilders.boolQuery()) {
      case (acc, (field, value)) =>
        acc.must(QueryBuilders.matchQuery(s"fields.$field", value))
    }
    new SearchRequest()
      .source(new SearchSourceBuilder().query(q))
      .indices(index)
  }

  val query: MultiSearchRequest =
    new MultiSearchRequest().add(searchQuery).add(attemptsQuery)
}
