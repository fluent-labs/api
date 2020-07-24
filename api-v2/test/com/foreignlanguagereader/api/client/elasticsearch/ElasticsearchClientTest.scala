package com.foreignlanguagereader.api.client.elasticsearch

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.common.CircuitBreakerAttempt
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchRequest
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource,
  GenericDefinition
}
import com.foreignlanguagereader.api.domain.word.PartOfSpeech
import com.foreignlanguagereader.api.util.ElasticsearchTestUtil
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.bulk.{BulkRequest, BulkResponse}
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchRequest,
  MultiSearchResponse
}
import com.sksamuel.elastic4s.{
  ElasticError,
  Handler,
  HitReader,
  RequestFailure,
  RequestSuccess
}
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.FutureOutcome
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration

import scala.concurrent.Future

class ElasticsearchClientTest extends AsyncFunSpec with MockitoSugar {
  val index = "definition"
  val fields: Map[String, String] =
    Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3")
  val attemptsRefreshEligible = LookupAttempt(index, fields, 4)

  val fetchedDefinition = GenericDefinition(
    List("refetched"),
    "ipa",
    Some(PartOfSpeech.NOUN),
    Some(List("this was refetched")),
    Language.ENGLISH,
    Language.ENGLISH,
    DefinitionSource.MULTIPLE,
    "refetched"
  )

  val holder: ElasticsearchClientHolder = mock[ElasticsearchClientHolder]
  val client = new ElasticsearchClient(
    mock[Configuration],
    holder,
    ActorSystem("testActorSystem", ConfigFactory.load())
  )

  // These are necessary to avoid having to manually create the results.
  // They are pulled into ElasticsearchTestUtil.cacheQueryResponseFrom, and given the appropriate responses.
  implicit val definitionHitReader: HitReader[Definition] =
    mock[HitReader[Definition]]
  implicit val attemptsHitReader: HitReader[LookupAttempt] =
    mock[HitReader[LookupAttempt]]

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    Mockito.reset(holder)
    super.withFixture(test)
  }

  describe("an elasticsearch client") {
    it("can fetch results from a cache") {
      val response: MultiSearchResponse = ElasticsearchTestUtil
        .cacheQueryResponseFrom[Definition](
          Right(List(fetchedDefinition)),
          Right(attemptsRefreshEligible)
        )
      when(
        holder.execute[MultiSearchRequest, MultiSearchResponse](
          any[MultiSearchRequest]
        )(
          any[Handler[MultiSearchRequest, MultiSearchResponse]],
          any[Manifest[MultiSearchResponse]]
        )
      ).thenReturn(
        Future.successful(RequestSuccess(200, None, Map(), response))
      )

      client
        .getFromCache[Definition](
          List(
            ElasticsearchRequest(
              "definitions",
              Map(),
              () =>
                throw new IllegalStateException("Fetcher shouldn't be called"),
              5
            )
          )
        )
        .map(results => {
          val result = results(0)
          assert(result.contains(List(fetchedDefinition)))
        })
    }
    it("can handle cache misses") {
      val response: MultiSearchResponse = ElasticsearchTestUtil
        .cacheQueryResponseFrom[Definition](
          Right(List()),
          Right(attemptsRefreshEligible)
        )
      when(
        holder.execute[MultiSearchRequest, MultiSearchResponse](
          any[MultiSearchRequest]
        )(
          any[Handler[MultiSearchRequest, MultiSearchResponse]],
          any[Manifest[MultiSearchResponse]]
        )
      ).thenReturn(
        Future.successful(RequestSuccess(200, None, Map(), response))
      )

      client
        .getFromCache[Definition](
          List(
            ElasticsearchRequest(
              "definitions",
              Map(),
              () =>
                Future.successful(
                  CircuitBreakerAttempt(Some(List(fetchedDefinition)))
              ),
              5
            )
          )
        )
        .map(results => {
          // Make sure we saved this back to the cache
          // Matchers are OK because the specifics of the request is tested in the search states
          verify(holder)
            .execute[BulkRequest, BulkResponse](any(classOf[BulkRequest]))(
              any[Handler[BulkRequest, BulkResponse]],
              any[Manifest[BulkResponse]]
            )

          val result = results(0)
          assert(result.contains(List(fetchedDefinition)))
        })
    }
    describe("gracefully handles failures") {
//      it(
//        "retries indexing attempts which happened while the circuit breaker was open."
//      )
//      it("retries requests one by one if bulk requests failed")
      it("can handle failures when searching from elasticsearch") {
        when(
          holder.execute[MultiSearchRequest, MultiSearchResponse](
            any[MultiSearchRequest]
          )(
            any[Handler[MultiSearchRequest, MultiSearchResponse]],
            any[Manifest[MultiSearchResponse]]
          )
        ).thenReturn(
          Future.successful(
            RequestFailure(
              200,
              None,
              Map(),
              new ElasticError(
                "",
                "You messed up",
                None,
                None,
                None,
                List(),
                None
              )
            )
          )
        )

        client
          .getFromCache[Definition](
            List(
              ElasticsearchRequest(
                "definitions",
                Map(),
                () =>
                  Future.successful(
                    CircuitBreakerAttempt(Some(List(fetchedDefinition)))
                ),
                5
              )
            )
          )
          .map(results => {
            val result = results(0)
            assert(result.contains(List(fetchedDefinition)))
          })
      }
    }
  }
}
