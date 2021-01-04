package com.foreignlanguagereader.domain.client.elasticsearch

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.domain.metrics.{Metric, MetricsReporter}
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{
  MultiSearchRequest,
  MultiSearchResponse,
  SearchRequest,
  SearchResponse
}
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.elasticsearch.search.SearchHit
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

// $COVERAGE-OFF$
/**
  * Lower level elasticsearch client. We implement other logic on top of this.
  * @param config An already configured elasticsearch client.
  * @param system Thread pool setup for client
  */
@Singleton
class ElasticsearchClient @Inject() (
    config: ElasticsearchClientConfig,
    metrics: MetricsReporter,
    val system: ActorSystem
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("elasticsearch-context")
  val javaClient: RestHighLevelClient = config.getClient
  val breaker = new Circuitbreaker(system, ec, "Elasticsearch")
  val logger: Logger = Logger(this.getClass)

  val indexLabel = "index"
  def index(
      request: IndexRequest
  ): Future[CircuitBreakerResult[IndexResponse]] = {
    metrics.report(Metric.ELASTICSEARCH_CALLS, indexLabel)
    breaker.withBreaker(e => {
      logger.error(
        s"Failed to index document on index(es) ${request.indices().mkString(",")}: ${request.sourceAsMap()}",
        e
      )
      metrics.report(Metric.ELASTICSEARCH_FAILURES, indexLabel)
    }) {
      Future { javaClient.index(request, RequestOptions.DEFAULT) }
    }
  }

  val bulkLabel = "bulk"
  def bulk(
      request: BulkRequest
  ): Future[CircuitBreakerResult[BulkResponse]] = {
    metrics.report(Metric.ELASTICSEARCH_CALLS, bulkLabel)
    breaker.withBreaker(e => {
      logger.error("Failed bulk request", e)
      metrics.report(Metric.ELASTICSEARCH_FAILURES, bulkLabel)
    }) {
      Future { javaClient.bulk(request, RequestOptions.DEFAULT) }
    }
  }

  val searchLabel = "search"
  def search[T](
      request: SearchRequest
  )(implicit
      reads: Reads[T]
  ): Future[CircuitBreakerResult[Map[String, T]]] = {
    metrics.report(Metric.ELASTICSEARCH_CALLS, searchLabel)
    Nested
      .apply(
        breaker
          .withBreaker(e => {
            logger.error(
              s"Failed to search on index(es) ${request
                .indices()
                .mkString(",")}: ${request.source().query().toString}",
              e
            )
            metrics.report(Metric.ELASTICSEARCH_FAILURES, searchLabel)
          })(Future { javaClient.search(request, RequestOptions.DEFAULT) })
      )
      .map(response => getResultsFromSearchResponse(response))
      .value
  }

  val multisearchLabel = "multisearch"
  def doubleSearch[T, U](
      request: MultiSearchRequest
  )(implicit
      readsT: Reads[T],
      readsU: Reads[U]
  ): Future[CircuitBreakerResult[Option[(Map[String, T], Map[String, U])]]] = {
    metrics.report(Metric.ELASTICSEARCH_CALLS, multisearchLabel)
    Nested
      .apply[Future, CircuitBreakerResult, MultiSearchResponse](
        breaker.withBreaker(e => {
          logger.error("Failed to multisearch", e)
          metrics.report(Metric.ELASTICSEARCH_FAILURES, multisearchLabel)
        })(Future {
          javaClient.msearch(request, RequestOptions.DEFAULT)
        })
      )
      .map(response => {
        Try {
          logger.info(s"Received result from multisearch: $response")
          if (response.getResponses.length === 2) {
            val first =
              getResultsFromSearchResponse[T](
                response.getResponses.head.getResponse
              )
            val second = getResultsFromSearchResponse[U](
              response.getResponses.tail.head.getResponse
            )
            Some((first, second))
          } else {
            None
          }
        } match {
          case Success(result) => result
          case Failure(e) =>
            logger.error(s"Failed to multisearch: ${e.getMessage}", e)
            None
        }
      })
      .value
  }

  def createIndex(index: String): Unit = {
    javaClient
      .indices()
      .create(new CreateIndexRequest(index), RequestOptions.DEFAULT)
  }

  def createIndexes(indexes: List[String]): Unit = indexes.foreach(createIndex)

  // Parses responses from elasticsearch

  private[this] def getResultsFromSearchHit[T](
      hit: SearchHit
  )(implicit reads: Reads[T]): Option[(String, T)] =
    Json.parse(hit.getSourceAsString).validate[T] match {
      case JsSuccess(value, _) => Some(hit.getId -> value)
      case JsError(errors) =>
        val errs = errors
          .map {
            case (path, e) => s"At path $path: ${e.mkString(", ")}"
          }
          .mkString("\n")
        logger.error(
          s"Failed to parse results from elasticsearch due to errors: $errs"
        )
        None
    }

  private[this] def getResultsFromSearchResponse[T](
      response: SearchResponse
  )(implicit reads: Reads[T]): Map[String, T] = {
    logger.info(s"Getting results from search response: $response")
    val hits =
      response.getHits.getHits.toList.map(hit => getResultsFromSearchHit(hit))
    hits.flatten.toMap
  }

  def onClose(body: => Unit): Unit = breaker.onClose(body)
}
// $COVERAGE-ON$
