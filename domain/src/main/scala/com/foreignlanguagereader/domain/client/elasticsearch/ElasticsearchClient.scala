package com.foreignlanguagereader.domain.client.elasticsearch

import akka.actor.ActorSystem
import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerResult,
  Circuitbreaker
}
import javax.inject.{Inject, Singleton}
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
    val system: ActorSystem
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("elasticsearch-context")
  val javaClient: RestHighLevelClient = config.getClient
  val breaker = new Circuitbreaker(system, ec, "Elasticsearch")
  val logger: Logger = Logger(this.getClass)

  def index(
      request: IndexRequest
  ): Future[CircuitBreakerResult[IndexResponse]] =
    breaker.withBreaker(
      s"Failed to index document on index(es) ${request.indices().mkString(",")}: ${request.sourceAsMap()}"
    ) {
      Future { javaClient.index(request, RequestOptions.DEFAULT) }
    }

  def bulk(
      request: BulkRequest
  ): Future[CircuitBreakerResult[BulkResponse]] = {
    breaker.withBreaker("Failed bulk request") {
      Future { javaClient.bulk(request, RequestOptions.DEFAULT) }
    }
  }

  def search[T](
      request: SearchRequest
  )(implicit
      reads: Reads[T]
  ): Future[CircuitBreakerResult[Map[String, T]]] = {
    Nested
      .apply(
        breaker
          .withBreaker(
            s"Failed to search on index(es) ${request.indices().mkString(",")}: ${request.source().query().toString}"
          )(Future { javaClient.search(request, RequestOptions.DEFAULT) })
      )
      .map(response => getResultsFromSearchResponse(response))
      .value
  }

  def doubleSearch[T, U](
      request: MultiSearchRequest
  )(implicit
      readsT: Reads[T],
      readsU: Reads[U]
  ): Future[CircuitBreakerResult[Option[(Map[String, T], Map[String, U])]]] =
    Nested
      .apply[Future, CircuitBreakerResult, MultiSearchResponse](
        breaker.withBreaker("Failed to multisearch")(Future {
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
