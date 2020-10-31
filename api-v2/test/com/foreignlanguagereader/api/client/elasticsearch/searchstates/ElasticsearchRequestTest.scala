package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.common.CircuitBreakerAttempt
import com.foreignlanguagereader.domain.internal.definition.Definition
import com.sksamuel.elastic4s.ElasticDsl.{boolQuery, matchQuery, multi, search}
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.Future

class ElasticsearchRequestTest extends AnyFunSpec {
  describe("an elasticsearch request") {
    val index = "test"
    val searchQuery = boolQuery()
      .must(
        matchQuery("field1", "value1"),
        matchQuery("field2", "value2"),
        matchQuery("field3", "value 3")
      )
    val attemptsQuery = boolQuery()
      .must(
        matchQuery("fields.field1", "value1"),
        matchQuery("fields.field2", "value2"),
        matchQuery("fields.field3", "value 3")
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
      request.query == multi(
        search(index).query(searchQuery),
        search("attempts").query(attemptsQuery)
      )
    }
  }
}
