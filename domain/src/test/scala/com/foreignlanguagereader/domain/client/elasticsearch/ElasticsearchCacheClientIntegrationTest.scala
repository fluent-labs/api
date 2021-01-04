package com.foreignlanguagereader.domain.client.elasticsearch

import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.typesafe.config.ConfigFactory
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.tags.Slow
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.{Application, Configuration}

@Slow
class ElasticsearchCacheClientIntegrationTest
    extends AsyncFunSpec
    with MockitoSugar {
  val playConfig: Configuration = Configuration(ConfigFactory.load("test.conf"))
  val application: Application = new GuiceApplicationBuilder()
    .configure(playConfig)
    .build()
  val metricsMock: MetricsReporter = mock[MetricsReporter]

  describe("an elasticsearch client") {
    ignore("can index documents") {
      val config = new ElasticsearchClientConfig(
        playConfig,
        application.coordinatedShutdown,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val client =
        new ElasticsearchClient(config, metricsMock, application.actorSystem)
      client.createIndex("indexTest")

      val attempt = LookupAttempt("definitions", Map("field1" -> "value1"), 1)
      val indexRequest = new IndexRequest()
        .source(Json.toJson(attempt).toString(), XContentType.JSON)
        .index("indexTest")

      val query = QueryBuilders
        .boolQuery()
        .must(QueryBuilders.matchQuery("fields.field1", "value1"))
      val searchRequest = new SearchRequest()
        .source(new SearchSourceBuilder().query(query))
        .indices("indexTest")

      client
        .index(indexRequest)
        .flatMap {
          case CircuitBreakerNonAttempt() =>
            fail("Indexing failed because circuit breaker was closed")
          case CircuitBreakerFailedAttempt(e) =>
            fail(s"Indexing failed because of error: ${e.getMessage}", e)
          case CircuitBreakerAttempt(_) =>
            // Give elasticsearch five seconds to finish indexing before we search
            Thread.sleep(5000)
            client.search[LookupAttempt](searchRequest).map {
              case CircuitBreakerNonAttempt() =>
                fail("Searching failed because circuit breaker was closed")
              case CircuitBreakerFailedAttempt(e) =>
                fail(s"Searching failed because of error: ${e.getMessage}", e)
              case CircuitBreakerAttempt(result) =>
                assert(result.size == 1)
                assert(result.values.head == attempt)
            }
        }
    }
  }
}
