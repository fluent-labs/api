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
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.{
  RequestOptions,
  RestClient,
  RestHighLevelClient
}
import org.testcontainers.elasticsearch.ElasticsearchContainer
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

// Holder to enable easy mocking.
// $COVERAGE-OFF$
@Singleton
class ElasticsearchClientHolder @Inject() (config: Configuration) {
  val isLocal: Boolean = config.get[Boolean]("local")
  val httpHost: HttpHost = if (isLocal) {
    val container = new ElasticsearchContainer(
      "docker.elastic.co/elasticsearch/elasticsearch:7.9.3"
    )
    container.start()
    HttpHost.create(container.getHttpHostAddress)
  } else {
    val scheme = config.get[String]("elasticsearch.scheme")
    val url = config.get[String]("elasticsearch.url")
    val port = config.get[Int]("elasticsearch.port")
    new HttpHost(url, port, scheme)
  }

  val javaClient = new RestHighLevelClient(
    RestClient.builder(httpHost)
  )

  if (isLocal) {
    List("attempts", "definitions")
      .map(index => new CreateIndexRequest(index))
      .foreach(request =>
        javaClient.indices().create(request, RequestOptions.DEFAULT)
      )
  }

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

  def multiSearch(
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
    Option.apply(insertionQueue.poll())

  def hasMore: Boolean = !insertionQueue.isEmpty
}
// $COVERAGE-ON$
