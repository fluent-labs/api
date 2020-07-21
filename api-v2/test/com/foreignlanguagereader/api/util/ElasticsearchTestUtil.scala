package com.foreignlanguagereader.api.util

import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.sksamuel.elastic4s.requests.common.Shards
import com.sksamuel.elastic4s.requests.searches._
import com.sksamuel.elastic4s.{ElasticError, Hit, HitReader}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}

import scala.util.{Random, Try}

/**
  * Helper library to get rid of boilerplate when testing elasticsearch responses
  */
object ElasticsearchTestUtil {
  val random = new Random()

  def cacheQueryResponseFrom[T](results: Either[Exception, Seq[T]],
                                attempts: Either[Exception, LookupAttempt],
  )(implicit hitReader: HitReader[T],
    attemptsHitReader: HitReader[LookupAttempt]): MultiSearchResponse = {

    val resultsResponse = results match {
      case Left(e) =>
        val result = mock(classOf[ElasticError])
        when(result.asException).thenReturn(e)
        Left(result)
      case Right(r) =>
        val hits = r
          .map(result => {
            val hit = mock(classOf[SearchHit])
            when(hitReader.read(hit)).thenReturn(Try(result))
            hit
          })
          .toArray
        Right(searchResponseFrom(hits))
    }
    val attemptsResponse = attempts match {
      case Left(e) =>
        val result = mock(classOf[ElasticError])
        when(result.asException).thenReturn(e)
        Left(result)
      case Right(a) =>
        when(attemptsHitReader.read(any(classOf[Hit]))).thenReturn(Try(a))
        val hit = mock(classOf[SearchHit])
        when(hit.id).thenReturn(random.nextString(8))
        Right(searchResponseFrom(Array(hit)))
    }

    MultiSearchResponse(
      List(
        MultisearchResponseItem(0, 200, resultsResponse),
        MultisearchResponseItem(0, 200, attemptsResponse)
      )
    )
  }

  def searchResponseFrom[T](hits: Array[SearchHit]) =
    SearchResponse(
      took = 5L,
      isTimedOut = false,
      isTerminatedEarly = false,
      suggest = Map(),
      Shards(1, 1, 0),
      None,
      Map(),
      SearchHits(Total(value = 5L, relation = ""), maxScore = 4, hits = hits)
    )
}
