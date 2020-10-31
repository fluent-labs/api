package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.ConcurrentLinkedQueue

import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchCacheRequest
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http.JavaClient
import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.concurrent.Future
import scala.collection.JavaConverters._

// Holder to enable easy mocking
@Singleton
class ElasticsearchClientHolder @Inject() (config: Configuration) {
  val elasticSearchUrl: String = config.get[String]("elasticsearch.url")
  val client = ElasticClient(JavaClient(ElasticProperties(elasticSearchUrl)))

  def execute[T, U](request: T)(implicit
      handler: Handler[T, U],
      manifest: Manifest[U]
  ): Future[Response[U]] =
    client.execute(request)

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
