package com.foreignlanguagereader.domain.client.elasticsearch

import cats.implicits._
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  DefinitionSource,
  EnglishDefinition
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
import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class ElasticsearchCacheClientTest extends AsyncFunSpec with MockitoSugar {
  val index = "definition"
  val fields: Map[String, String] =
    Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3")

  val fetchedDefinition: EnglishDefinition = EnglishDefinition(
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

  implicit val reads: Reads[EnglishDefinition] = Json.reads[EnglishDefinition]
  implicit val writes: Writes[EnglishDefinition] =
    Json.writes[EnglishDefinition]

  val dummyMultisearchResponse = new MultiSearchResponse(Array(), 2L)

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    Mockito.reset(queue)
    Mockito.reset(baseClient)
    super.withFixture(test)
  }

  def makeDoubleSearchResponse(
      definitions: Seq[EnglishDefinition],
      attempts: LookupAttempt
  ): Future[CircuitBreakerResult[Option[
    (
        Map[String, ElasticsearchCacheable[EnglishDefinition]],
        Map[String, LookupAttempt]
    )
  ]]] = {
    val defs = definitions
      .map(d => ElasticsearchCacheable(d, attempts.fields))
      .map(d => d.item.token -> d)
      .toMap
    val attempt = Map("dummyId" -> attempts)
    Future.apply(CircuitBreakerAttempt(Some((defs, attempt))))
  }

  describe("an elasticsearch caching client") {
    it("can fetch results from a cache") {
      when(
        baseClient.doubleSearch[ElasticsearchCacheable[
          EnglishDefinition
        ], LookupAttempt](
          any(classOf[MultiSearchRequest])
        )(
          any(classOf[Reads[ElasticsearchCacheable[EnglishDefinition]]]),
          any(classOf[Reads[LookupAttempt]])
        )
      ).thenReturn(
        makeDoubleSearchResponse(
          List(fetchedDefinition),
          LookupAttempt("definitions", Map(), 4)
        )
      )

      cacheClient
        .findFromCacheOrRefetch[EnglishDefinition](
          ElasticsearchSearchRequest(
            "definitions",
            Map(),
            () =>
              throw new IllegalStateException("Fetcher shouldn't be called"),
            5
          )
        )
        .map(results => {
          verify(queue, never)
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          assert(results.contains(fetchedDefinition))
        })
    }

    it("can handle cache misses") {
      when(
        baseClient.doubleSearch[ElasticsearchCacheable[
          EnglishDefinition
        ], LookupAttempt](
          any(classOf[MultiSearchRequest])
        )(
          any(classOf[Reads[ElasticsearchCacheable[EnglishDefinition]]]),
          any(classOf[Reads[LookupAttempt]])
        )
      ).thenReturn(
        makeDoubleSearchResponse(
          List(),
          LookupAttempt("definitions", Map(), 4)
        )
      )

      cacheClient
        .findFromCacheOrRefetch[EnglishDefinition](
          ElasticsearchSearchRequest(
            "definitions",
            Map(),
            () =>
              Future
                .successful(CircuitBreakerAttempt(List(fetchedDefinition))),
            5
          )
        )
        .map(results => {
          verify(queue)
            .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

          assert(results.contains(fetchedDefinition))
        })
    }

    describe("gracefully handles failures") {
      it("can handle failures when searching from elasticsearch") {
        when(
          baseClient.doubleSearch[EnglishDefinition, LookupAttempt](
            any(classOf[MultiSearchRequest])
          )(
            any(classOf[Reads[EnglishDefinition]]),
            any(classOf[Reads[LookupAttempt]])
          )
        ).thenReturn(
          Future.apply(
            CircuitBreakerFailedAttempt(
              new IllegalStateException("Uh oh")
            )
          )
        )

        cacheClient
          .findFromCacheOrRefetch[EnglishDefinition](
            ElasticsearchSearchRequest(
              "definitions",
              Map(),
              () =>
                Future
                  .successful(CircuitBreakerAttempt(List(fetchedDefinition))),
              5
            )
          )
          .map(results => {
            verify(queue, never)
              .addInsertsToQueue(any(classOf[Seq[ElasticsearchCacheRequest]]))

            assert(results.contains(fetchedDefinition))
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
          Future.successful(CircuitBreakerAttempt(mock[BulkResponse]))
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
          Future.apply(
            CircuitBreakerFailedAttempt(
              new IllegalStateException("Please try again")
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
