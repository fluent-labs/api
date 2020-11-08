package com.foreignlanguagereader.domain.client.elasticsearch

import java.util.concurrent.ConcurrentLinkedQueue

import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.ElasticsearchCacheRequest
import javax.inject.{Inject, Singleton}
import org.apache.http.HttpHost
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{
  MultiSearchRequest,
  MultiSearchResponse,
  SearchRequest,
  SearchResponse
}
import org.elasticsearch.client.{
  RequestOptions,
  RestClient,
  RestHighLevelClient
}
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

// Holder to enable easy mocking
@Singleton
class ElasticsearchClientHolder @Inject() (config: Configuration) {
  val elasticSearchUrl: String = config.get[String]("elasticsearch.url")

  val javaClient = new RestHighLevelClient(
    RestClient.builder(
      new HttpHost(elasticSearchUrl, 9200, "http")
    )
  )

  def index(
      request: IndexRequest
  )(implicit ec: ExecutionContext): Future[IndexResponse] =
    Future { javaClient.index(request, RequestOptions.DEFAULT) }

  def bulk(
      request: BulkRequest
  )(implicit ec: ExecutionContext): Future[BulkResponse] =
    Future { javaClient.bulk(request, RequestOptions.DEFAULT) }

  def search(
      request: SearchRequest
  )(implicit ec: ExecutionContext): Future[SearchResponse] =
    Future { javaClient.search(request, RequestOptions.DEFAULT) }

  def multisearch(
      request: MultiSearchRequest
  )(implicit ec: ExecutionContext): Future[MultiSearchResponse] =
    Future { javaClient.msearch(request, RequestOptions.DEFAULT) }

  // Handles retrying of queue

  def addInsertToQueue(request: ElasticsearchCacheRequest): Unit = {
    // Result is always true, it's java tech debt.
    val _ = insertionQueue.add(request)
  }

  def addInsertsToQueue(requests: Seq[ElasticsearchCacheRequest]): Unit = {
    // Result is always true, it's java tech debt.
    val _ = insertionQueue.addAll(requests.asJava)
  }

  // Holds inserts that weren't performed due to the circuit breaker being closed
  // Mutability is a risk but a decent tradeoff because the cost of losing an insert or two is low
  // But setting up actors makes this much more complicated and is not worth it IMO
  private[this] val insertionQueue
      : ConcurrentLinkedQueue[ElasticsearchCacheRequest] =
    new ConcurrentLinkedQueue()

  def nextInsert(): Option[ElasticsearchCacheRequest] =
    insertionQueue.poll() match {
      case null                            => None // scalastyle:off
      case item: ElasticsearchCacheRequest => Some(item)
    }

  def hasMore: Boolean = !insertionQueue.isEmpty
}
