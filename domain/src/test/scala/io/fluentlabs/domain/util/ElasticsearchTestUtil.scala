package io.fluentlabs.domain.util

import io.fluentlabs.content.types.internal.ElasticsearchCacheable
import io.fluentlabs.domain.client.elasticsearch.LookupAttempt
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.xcontent.XContentType
import play.api.libs.json.{Json, Writes}

/** Helper library to get rid of boilerplate when testing elasticsearch responses
  */
object ElasticsearchTestUtil {
  def lookupIndexRequestFrom(attempt: LookupAttempt): IndexRequest = {
    indexRequestFrom("attempts", attempt)
  }
  def lookupUpdateRequestFrom(
      id: String,
      attempt: LookupAttempt
  ): UpdateRequest = {
    new UpdateRequest("attempts", id)
      .upsert(Json.toJson(attempt).toString(), XContentType.JSON)
  }

  def indexRequestFrom[T](
      index: String,
      item: T,
      fields: Map[String, String]
  )(implicit
      writes: Writes[T]
  ): IndexRequest = {
    val cacheable = ElasticsearchCacheable(item, fields)
    indexRequestFrom(index, cacheable)
  }

  private[this] def indexRequestFrom[T](
      index: String,
      item: T
  )(implicit
      writes: Writes[T]
  ): IndexRequest = {
    new IndexRequest()
      .index(index)
      .source(Json.toJson(item).toString(), XContentType.JSON)
  }
}
