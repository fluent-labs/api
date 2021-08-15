package io.fluentlabs.domain.client.elasticsearch.searchstates

import cats.syntax.all._
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest

case class ElasticsearchCacheRequest(
    requests: List[Either[IndexRequest, UpdateRequest]],
    retried: Boolean = false
) {

  // We attempt to do updates in bulk, assuming all requests are good
  // If a bulk fails, we should retry each individually.
  // Why? If there is one bad request, the other four might be good.
  lazy val retries: Option[List[ElasticsearchCacheRequest]] =
    if (retried) None
    else
      requests
        .map(request =>
          ElasticsearchCacheRequest(List(request), retried = true)
        )
        .some

}
object ElasticsearchCacheRequest {
  val maxConcurrentInserts = 5
  def fromRequests(
      requests: List[Either[IndexRequest, UpdateRequest]]
  ): List[ElasticsearchCacheRequest] = {
    requests
      .grouped(maxConcurrentInserts)
      .map(batch => ElasticsearchCacheRequest(batch))
      .toList
  }
}
