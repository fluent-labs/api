package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.sksamuel.elastic4s.requests.bulk.BulkCompatibleRequest

case class ElasticsearchCacheRequest(requests: Seq[BulkCompatibleRequest],
                                     retried: Boolean = false) {

  // We attempt to do updates in bulk, assuming all requests are good
  // If a bulk fails, we should retry each individually.
  // Why? If there is one bad request, the other four might be good.
  lazy val retries: Option[Seq[ElasticsearchCacheRequest]] =
    if (retried) None
    else
      Some(
        requests.map(
          request => ElasticsearchCacheRequest(List(request), retried = true)
        )
      )

}
object ElasticsearchCacheRequest {
  val maxConcurrentInserts = 5
  def fromRequests(
    requests: Seq[BulkCompatibleRequest]
  ): Seq[ElasticsearchCacheRequest] = {
    requests
      .grouped(maxConcurrentInserts)
      .map(batch => ElasticsearchCacheRequest(batch))
      .toSeq
  }
}
