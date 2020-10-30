package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import cats.implicits._
import com.sksamuel.elastic4s.requests.bulk.BulkCompatibleRequest

case class ElasticsearchCacheRequest(requests: List[BulkCompatibleRequest],
                                     retried: Boolean = false) {

  // We attempt to do updates in bulk, assuming all requests are good
  // If a bulk fails, we should retry each individually.
  // Why? If there is one bad request, the other four might be good.
  lazy val retries: Option[List[ElasticsearchCacheRequest]] =
    if (retried) None
    else
      requests
        .map(
          request => ElasticsearchCacheRequest(List(request), retried = true)
        )
        .some

}
object ElasticsearchCacheRequest {
  val maxConcurrentInserts = 5
  def fromRequests(
    requests: List[BulkCompatibleRequest]
  ): List[ElasticsearchCacheRequest] = {
    requests
      .grouped(maxConcurrentInserts)
      .map(batch => ElasticsearchCacheRequest(batch))
      .toList
  }
}
