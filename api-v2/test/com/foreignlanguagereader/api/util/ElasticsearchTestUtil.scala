package com.foreignlanguagereader.api.util

import com.sksamuel.elastic4s.requests.common.Shards
import com.sksamuel.elastic4s.requests.searches._
import com.sksamuel.elastic4s.{ElasticError, HitReader}
import org.mockito.Mockito.{mock, when}

/**
  * Helper library to get rid of boilerplate when testing elasticsearch responses
  */
object ElasticsearchTestUtil {
  def multisearchResponseFrom(
    searches: List[Either[ElasticError, SearchResponse]]
  ): MultiSearchResponse = {
    MultiSearchResponse(
      searches.map(search => MultisearchResponseItem(0, 200, search))
    )
  }

  def searchResponseFrom[T](hits: List[T])(implicit hitReader: HitReader[T]) =
    SearchResponse(
      took = 5L,
      isTimedOut = false,
      isTerminatedEarly = false,
      suggest = Map(),
      Shards(1, 1, 0),
      None,
      Map(),
      SearchHits(
        Total(value = 5L, relation = ""),
        maxScore = 4,
        hits = searchHitsFrom(hits)
      )
    )
  def searchHitsFrom[T](
    hits: List[T]
  )(implicit hitReader: HitReader[T]): Array[SearchHit] = {
    hits
      .map(item => {
        val mockHit = mock(classOf[SearchHit])
        when(mockHit.to).thenReturn(item)
        mockHit
      })
      .toArray
  }
}
