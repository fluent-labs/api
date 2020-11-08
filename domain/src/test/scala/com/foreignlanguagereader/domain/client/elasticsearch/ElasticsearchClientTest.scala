package com.foreignlanguagereader.domain.client.elasticsearch

import akka.actor.ActorSystem
import cats.implicits._
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource,
  GenericDefinition
}
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.common.CircuitBreakerAttempt
import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.{
  ElasticsearchCacheRequest,
  ElasticsearchSearchRequest
}
import com.foreignlanguagereader.domain.util.ElasticsearchTestUtil
import com.typesafe.config.ConfigFactory
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.search.{MultiSearchRequest, MultiSearchResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.FutureOutcome
import org.scalatest.funspec.AsyncFunSpec
import play.api.Configuration

import scala.concurrent.Future

class ElasticsearchClientTest extends AsyncFunSpec with MockitoSugar {
  val index = "definition"
  val fields: Map[String, String] =
    Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3")
  val attemptsRefreshEligible: LookupAttempt = LookupAttempt(index, fields, 4)

  val fetchedDefinition: GenericDefinition = GenericDefinition(
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

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    Mockito.reset(holder)
    super.withFixture(test)
  }

  describe("an elasticsearch client") {
    it("can fetch results from a cache") {
      val response: MultiSearchResponse = ElasticsearchTestUtil
        .cacheQueryResponseFrom[Definition](
          Some(List(fetchedDefinition)),
          Some(attemptsRefreshEligible)
        )
      when(
        holder.multisearch(any(classOf[MultiSearchRequest]))
      ).thenReturn(
        Future.successful(response)
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
          verify(holder, never)
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          val result = results.head
          assert(result.contains(fetchedDefinition))
        })
    }
    it("can handle cache misses") {
      val response: MultiSearchResponse = ElasticsearchTestUtil
        .cacheQueryResponseFrom[Definition](
          Some(List()),
          Some(attemptsRefreshEligible)
        )
      when(
        holder.multisearch(
          any(classOf[MultiSearchRequest])
        )
      ).thenReturn(
        Future.successful(response)
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

          val result = results.head
          assert(result.contains(fetchedDefinition))
        })
    }
    describe("gracefully handles failures") {
      it("can handle failures when searching from elasticsearch") {
        when(
          holder.multisearch(
            any(classOf[MultiSearchRequest])
          )
        ).thenThrow(new IllegalStateException("Uh oh"))

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
            verify(holder, never)
              .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

            val result = results.head
            assert(result.contains(fetchedDefinition))
          })
      }
    }
    describe("can save") {
      val indexRequests = (1 to 5)
        .map(number =>
          ElasticsearchTestUtil
            .lookupIndexRequestFrom(
              LookupAttempt(
                index = "test",
                fields = Map(
                  s"Field ${1 * number}" -> s"Value ${1 * number}",
                  s"Field ${2 * number}" -> s"Value ${2 * number}"
                ),
                count = number
              )
            )
            .asLeft
        )
        .toList
      val requests = ElasticsearchCacheRequest.fromRequests(indexRequests)
      val request = requests.head

      it("can save correctly") {
        when(
          holder.bulk(any(classOf[BulkRequest]))
        ).thenReturn(
          Future.successful(mock[BulkResponse])
        )

        client
          .save(request)
          .map(_ => {
            verify(holder, never)
              .addInsertsToQueue(request.retries.getOrElse(List()))
            succeed
          })
      }
      ignore("retries requests one by one if bulk requests failed") {
        when(
          holder.bulk(
            any(classOf[BulkRequest])
          )
        ).thenThrow(new IllegalStateException("Please try again"))

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
