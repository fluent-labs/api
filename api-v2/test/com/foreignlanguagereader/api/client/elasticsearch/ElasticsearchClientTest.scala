package com.foreignlanguagereader.api.client.elasticsearch

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.common.CircuitBreakerAttempt
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.{
  ElasticsearchCacheRequest,
  ElasticsearchSearchRequest
}
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.foreignlanguagereader.api.util.ElasticsearchTestUtil
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.{
  Definition,
  DefinitionSource,
  GenericDefinition
}
import com.sksamuel.elastic4s.ElasticDsl.{bulk, indexInto}
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.bulk.{BulkRequest, BulkResponse}
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchRequest,
  MultiSearchResponse
}
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
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
    PartOfSpeech.NOUN,
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
          any(classOf[MultiSearchRequest])
        )(
          any(classOf[Handler[MultiSearchRequest, MultiSearchResponse]]),
          any(classOf[Manifest[MultiSearchResponse]])
        )
      ).thenReturn(
        Future.successful(RequestSuccess(200, None, Map(), response))
      )

      client
        .findFromCacheOrRefetch[Definition](
          List(
            ElasticsearchSearchRequest(
              "definitions",
              Map(),
              () =>
                throw new IllegalStateException("Fetcher shouldn't be called"),
              5
            )
          )
        )
        .map(results => {
          verify(holder, never())
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          val result = results(0)
          assert(result.contains(fetchedDefinition))
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
          any(classOf[MultiSearchRequest])
        )(
          any(classOf[Handler[MultiSearchRequest, MultiSearchResponse]]),
          any(classOf[Manifest[MultiSearchResponse]])
        )
      ).thenReturn(
        Future.successful(RequestSuccess(200, None, Map(), response))
      )

      client
        .findFromCacheOrRefetch[Definition](
          List(
            ElasticsearchSearchRequest(
              "definitions",
              Map(),
              () =>
                Future
                  .successful(CircuitBreakerAttempt(List(fetchedDefinition))),
              5
            )
          )
        )
        .map(results => {
          verify(holder)
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          val result = results(0)
          assert(result.contains(fetchedDefinition))
        })
    }
    describe("gracefully handles failures") {
      it("can handle failures when searching from elasticsearch") {
        when(
          holder.execute[MultiSearchRequest, MultiSearchResponse](
            any(classOf[MultiSearchRequest])
          )(
            any(classOf[Handler[MultiSearchRequest, MultiSearchResponse]]),
            any(classOf[Manifest[MultiSearchResponse]])
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
          .findFromCacheOrRefetch[Definition](
            List(
              ElasticsearchSearchRequest(
                "definitions",
                Map(),
                () =>
                  Future
                    .successful(CircuitBreakerAttempt(List(fetchedDefinition))),
                5
              )
            )
          )
          .map(results => {
            verify(holder, never())
              .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

            val result = results(0)
            assert(result.contains(fetchedDefinition))
          })
      }
    }
    describe("can save") {
      val indexRequests = (1 to 5)
        .map(number =>
          indexInto("attempts")
            .doc(
              LookupAttempt(
                index = "test",
                fields = Map(
                  s"Field ${1 * number}" -> s"Value ${1 * number}",
                  s"Field ${2 * number}" -> s"Value ${2 * number}"
                ),
                count = number
              )
            )
        )
        .toList
      val requests = ElasticsearchCacheRequest.fromRequests(indexRequests)
      val request = requests(0)

      it("can save correctly") {
        when(
          holder.execute[BulkRequest, BulkResponse](any(classOf[BulkRequest]))(
            any(classOf[Handler[BulkRequest, BulkResponse]]),
            any(classOf[Manifest[BulkResponse]])
          )
        ).thenReturn(
          Future
            .successful(
              RequestSuccess(
                200,
                None,
                Map(),
                BulkResponse(0, errors = false, List())
              )
            )
        )

        client
          .save(request)
          .map(_ => {
            verify(holder, never())
              .addInsertsToQueue(request.retries.getOrElse(List()))
            succeed
          })
      }
      ignore("retries requests one by one if bulk requests failed") {
        when(
          holder.execute[BulkRequest, BulkResponse](
            mockitoEq[BulkRequest](bulk(request.requests))
          )(
            any(classOf[Handler[BulkRequest, BulkResponse]]),
            any(classOf[Manifest[BulkResponse]])
          )
        ).thenReturn(
          Future.successful(
            RequestFailure(
              200,
              None,
              Map(),
              new ElasticError(
                "",
                "Please try again",
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
          .save(request)
          .map(_ => {
            verify(holder)
              .addInsertsToQueue(request.retries.getOrElse(List()))
            succeed
          })
      }
    }
  }
}
