package com.foreignlanguagereader.domain.client.elasticsearch

import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConverters._

import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.ElasticsearchCacheRequest

class CacheQueue {
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
}
