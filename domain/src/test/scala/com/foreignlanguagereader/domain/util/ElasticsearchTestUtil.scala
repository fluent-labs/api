package com.foreignlanguagereader.domain.util

import com.foreignlanguagereader.domain.client.elasticsearch.LookupAttempt
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.{MultiSearchResponse, SearchResponse}
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.search.{SearchHit, SearchHits}
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{Json, Writes}

/**
  * Helper library to get rid of boilerplate when testing elasticsearch responses
  */
object ElasticsearchTestUtil {
  def getSearchResponseFrom[T](
      items: Seq[T]
  )(implicit writes: Writes[T]): SearchResponse = {
    def hits = mock(classOf[SearchHits])
    when(hits.getHits)
      .thenReturn(items.map(item => searchHitFrom(item)).toArray)

    def response = mock(classOf[SearchResponse])
    when(response.getHits).thenReturn(hits)

    response
  }

  def searchHitFrom[T](item: T)(implicit writes: Writes[T]): SearchHit = {
    val hit = mock(classOf[SearchHit])
    when(hit.getSourceAsString).thenReturn(Json.toJson(item).toString())
    hit
  }

  def getMultiSearchItemFrom[T](
      items: Seq[T]
  )(implicit writes: Writes[T]): MultiSearchResponse.Item = {
    val item = mock(classOf[MultiSearchResponse.Item])
    when(item.isFailure).thenReturn(false)
    when(item.getResponse).thenReturn(getSearchResponseFrom(items))
    item
  }

  def getFailedMultiSearchItem: MultiSearchResponse.Item = {
    val item = mock(classOf[MultiSearchResponse.Item])
    when(item.isFailure).thenReturn(true)
    item
  }

  def cacheQueryResponseFrom[T](
      results: Option[Seq[T]],
      attempts: Option[LookupAttempt]
  )(implicit writes: Writes[T]): MultiSearchResponse = {
    val resultsResponse = results
      .map(r => getMultiSearchItemFrom(r))
      .getOrElse(getFailedMultiSearchItem)
    val attemptsResponse = attempts
      .map(a => getMultiSearchItemFrom(Array(a)))
      .getOrElse(getFailedMultiSearchItem)

    val response = mock(classOf[MultiSearchResponse])
    when(response.getResponses)
      .thenReturn(Array(resultsResponse, attemptsResponse))
    response
  }

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

  def indexRequestFrom[T](index: String, item: T)(implicit
      writes: Writes[T]
  ): IndexRequest = {
    new IndexRequest()
      .index(index)
      .source(Json.toJson(item).toString(), XContentType.JSON)
  }
}
