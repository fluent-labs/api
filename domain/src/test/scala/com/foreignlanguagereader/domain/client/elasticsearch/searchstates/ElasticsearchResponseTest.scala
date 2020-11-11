package com.foreignlanguagereader.domain.client.elasticsearch.searchstates

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource,
  GenericDefinition
}
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.client.elasticsearch.LookupAttempt
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

import scala.concurrent.Future

class ElasticsearchResponseTest extends AsyncFunSpec with MockitoSugar {
  val index: String = "definition"
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
  val attemptsRefreshEligible: LookupAttempt = LookupAttempt(index, fields, 4)
  val attemptsRefreshIneligible: LookupAttempt = LookupAttempt(index, fields, 7)

  val responseBase: ElasticsearchSearchResponse[Definition] =
    ElasticsearchSearchResponse[Definition](
      index = index,
      fields = fields,
      fetcher =
        () => Future.successful(CircuitBreakerAttempt(List(fetchedDefinition))),
      maxFetchAttempts = 5,
      response = CircuitBreakerNonAttempt()
    )

  def makeDoubleSearchResponse(
      definitions: Seq[Definition],
      attempts: Option[LookupAttempt]
  ): CircuitBreakerResult[Option[
    (Map[String, Definition], Map[String, LookupAttempt])
  ]] = {
    val defs = definitions.map(d => d.token -> d).toMap
    val attempt = attempts match {
      case Some(a) => Map("dummyId" -> a)
      case None    => Map[String, LookupAttempt]()
    }
    CircuitBreakerAttempt(Some((defs, attempt)))
  }

  describe("an elasticsearch response") {
    describe("where data was found in elasticsearch") {

      val response = responseBase.copy(response =
        makeDoubleSearchResponse(
          List(fetchedDefinition),
          Some(LookupAttempt("definitions", Map(), 3))
        )
      )

      it("contains the response") {
        assert(response.elasticsearchResult.contains(Seq(fetchedDefinition)))
      }

      it("does not call the fetcher") {
        response.getResultOrFetchFromSource.map(result => {
          assert(!result.refetched)
        })
      }
    }

    describe("where data was not found") {
      it("updates the fetch count if the fetcher fetched") {
        val response = responseBase.copy(response =
          makeDoubleSearchResponse(
            List(),
            Some(LookupAttempt("definitions", Map(), 4))
          )
        )
        assert(response.elasticsearchResult.isEmpty)

        response.getResultOrFetchFromSource.map(result => {
          assert(result.refetched)
          assert(result.result.contains(fetchedDefinition))
        })
      }

      it("updates the fetch count if the fetcher failed") {
        val fetcher: () => Future[CircuitBreakerResult[List[Definition]]] =
          () => Future.failed(new IllegalArgumentException("Oh no"))
        val response = responseBase.copy(
          response = makeDoubleSearchResponse(
            List(),
            Some(LookupAttempt("definitions", Map(), 4))
          ),
          fetcher = fetcher
        )
        assert(response.elasticsearchResult.isEmpty)

        response.getResultOrFetchFromSource.map(result => {
          assert(result.refetched)
          assert(result.result.isEmpty)
        })
      }

      it("does not update the fetch count if the fetcher did not fetch") {
        val response = responseBase.copy(
          response = makeDoubleSearchResponse(
            List(),
            Some(LookupAttempt("definitions", Map(), 4))
          ),
          fetcher = () =>
            Future.successful(CircuitBreakerNonAttempt[List[Definition]]())
        )
        assert(response.elasticsearchResult.isEmpty)

        response.getResultOrFetchFromSource.map(result => {
          assert(!result.refetched)
          assert(result.result.isEmpty)
        })
      }
    }

    describe("where fetching has failed too many times") {
      it("does not call the fetcher") {
        val response = responseBase.copy(response =
          makeDoubleSearchResponse(
            List(),
            Some(LookupAttempt("definitions", Map(), 7))
          )
        )
        assert(response.elasticsearchResult.isEmpty)

        response.getResultOrFetchFromSource.map(result => {
          assert(!result.refetched)
          assert(result.result.isEmpty)
        })
      }
    }

    describe("with errors") {
      it("fetches when the fetch count cannot be found") {
        val response =
          responseBase.copy(response = makeDoubleSearchResponse(List(), None))
        assert(response.elasticsearchResult.isEmpty)

        response.getResultOrFetchFromSource.map(result => {
          assert(result.refetched)
          assert(result.fetchCount == 1)
        })
      }

//      it("gracefully handles when elasticsearch fails") {
//        when(reader.getResultsFromSearch[Definition](dummyMultisearchResponse))
//          .thenReturn((None, 4, Some("dummyId")))
//        val response = responseBase.copy(response =
//          CircuitBreakerAttempt(dummyMultisearchResponse)
//        )
//        assert(response.elasticsearchResult.isEmpty)
//      }

      it("gracefully handles when elasticsearch is completely unavailable") {
        assert(responseBase.elasticsearchResult.isEmpty)

        responseBase.getResultOrFetchFromSource.map(result => {
          assert(result.refetched)
          assert(result.fetchCount == 1)
        })
      }
    }
  }
}
