package com.foreignlanguagereader.domain.client.elasticsearch.searchstates

import io.fluentlabs.content.types.internal.definition.Definition
import com.foreignlanguagereader.domain.client.circuitbreaker.CircuitBreakerAttempt
import org.elasticsearch.action.search.{MultiSearchRequest, SearchRequest}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.Future

class ElasticsearchRequestTest extends AnyFunSpec {
  describe("an elasticsearch request") {
    val index = "test"
    val searchQuery = new SearchRequest()
      .indices(index)
      .source(
        new SearchSourceBuilder().query(
          QueryBuilders
            .boolQuery()
            .must(QueryBuilders.matchQuery("field1", "value1"))
            .must(QueryBuilders.matchQuery("field2", "value2"))
            .must(QueryBuilders.matchQuery("field3", "value 3"))
        )
      )
    val attemptsQuery = new SearchRequest()
      .indices("attempts")
      .source(
        new SearchSourceBuilder().query(
          QueryBuilders
            .boolQuery()
            .must(QueryBuilders.matchQuery("fields.field1", "value1"))
            .must(QueryBuilders.matchQuery("fields.field2", "value2"))
            .must(QueryBuilders.matchQuery("fields.field3", "value 3"))
        )
      )
    val request = ElasticsearchSearchRequest[Definition](
      index,
      Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3"),
      () => Future.successful(CircuitBreakerAttempt(List())),
      maxFetchAttempts = 5
    )

    it("generates the correct elasticsearch query") {
      request.searchQuery == searchQuery
    }
    it("generates the correct attempts query") {
      request.attemptsQuery == attemptsQuery
    }
    it("generates the correct final query") {
      request.query == new MultiSearchRequest()
        .add(searchQuery)
        .add(attemptsQuery)
    }
  }
}
