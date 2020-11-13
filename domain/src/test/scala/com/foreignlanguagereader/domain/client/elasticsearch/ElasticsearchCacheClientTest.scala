package com.foreignlanguagereader.domain.client.elasticsearch

import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource,
  GenericDefinition
}
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.{
  ElasticsearchCacheRequest,
  ElasticsearchSearchRequest
}
import com.foreignlanguagereader.domain.util.ElasticsearchTestUtil
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.search.{MultiSearchRequest, MultiSearchResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.FutureOutcome
import org.scalatest.funspec.AsyncFunSpec
import play.api.libs.json.Reads

import scala.concurrent.{ExecutionContext, Future}

class ElasticsearchCacheClientTest extends AsyncFunSpec with MockitoSugar {
  val index = "definition"
  val fields: Map[String, String] =
    Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3")

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

  val baseClient: ElasticsearchClient = mock[ElasticsearchClient]
  val queue: CacheQueue = mock[CacheQueue]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val cacheClient = new ElasticsearchCacheClient(baseClient, queue, ec)

  val dummyMultisearchResponse = new MultiSearchResponse(Array(), 2L)

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    Mockito.reset(queue)
    super.withFixture(test)
  }

  def makeDoubleSearchResponse(
      definitions: Seq[Definition],
      attempts: LookupAttempt
  ): Nested[Future, CircuitBreakerResult, Option[
    (Map[String, Definition], Map[String, LookupAttempt])
  ]] = {
    val defs = definitions.map(d => d.token -> d).toMap
    val attempt = Map("dummyId" -> attempts)
    Nested(Future.apply(CircuitBreakerAttempt(Some((defs, attempt)))))
  }

  describe("an elasticsearch caching client") {
    ignore("can fetch results from a cache") {
      when(
        baseClient.doubleSearch[Definition, LookupAttempt](
          any(classOf[MultiSearchRequest])
        )(
          any(classOf[Reads[Definition]]),
          any(classOf[Reads[LookupAttempt]])
        )
      ).thenReturn(
        makeDoubleSearchResponse(
          List(fetchedDefinition),
          LookupAttempt("definitions", Map(), 4)
        )
      )

      cacheClient
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
          verify(queue, never)
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          val result = results.head
          assert(result.contains(fetchedDefinition))
        })
    }

    it("can handle cache misses") {
      when(
        baseClient.doubleSearch[Definition, LookupAttempt](
          any(classOf[MultiSearchRequest])
        )(
          any(classOf[Reads[Definition]]),
          any(classOf[Reads[LookupAttempt]])
        )
      ).thenReturn(
        makeDoubleSearchResponse(
          List(),
          LookupAttempt("definitions", Map(), 4)
        )
      )

      cacheClient
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
          verify(queue)
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          val result = results.head
          assert(result.contains(fetchedDefinition))
        })
    }

    describe("gracefully handles failures") {
      it("can handle failures when searching from elasticsearch") {
        when(
          baseClient.doubleSearch[Definition, LookupAttempt](
            any(classOf[MultiSearchRequest])
          )(
            any(classOf[Reads[Definition]]),
            any(classOf[Reads[LookupAttempt]])
          )
        ).thenReturn(
          Nested(
            Future.apply(
              CircuitBreakerFailedAttempt(
                new IllegalStateException("Uh oh")
              )
            )
          )
        )

        cacheClient
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
            verify(queue, never)
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
          baseClient.bulk(any(classOf[BulkRequest]))
        ).thenReturn(
          Nested(Future.successful(CircuitBreakerAttempt(mock[BulkResponse])))
        )

        cacheClient
          .save(request)
          .map(_ => {
            verify(queue, never)
              .addInsertsToQueue(request.retries.getOrElse(List()))
            succeed
          })
      }
      it("retries requests one by one if bulk requests failed") {
        when(
          baseClient.bulk(
            any(classOf[BulkRequest])
          )
        ).thenReturn(
          Nested(
            Future.apply(
              CircuitBreakerFailedAttempt(
                new IllegalStateException("Please try again")
              )
            )
          )
        )

        cacheClient
          .save(request)
          .map(_ => {
            verify(queue)
              .addInsertsToQueue(request.retries.getOrElse(List()))
            succeed
          })
      }
    }
  }
}
