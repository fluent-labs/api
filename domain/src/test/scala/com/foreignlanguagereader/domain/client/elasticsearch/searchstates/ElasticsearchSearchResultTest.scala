package com.foreignlanguagereader.domain.client.elasticsearch.searchstates

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.{
  DefinitionSource,
  EnglishDefinition
}
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.elasticsearch.LookupAttempt
import com.foreignlanguagereader.domain.util.ElasticsearchTestUtil
import org.elasticsearch.action.index.IndexRequest
import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.{Json, Writes}

class ElasticsearchSearchResultTest extends AnyFunSpec {
  val index: String = "definition"
  val fields: Map[String, String] =
    Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3")

  describe("an elasticsearch result") {
    describe("where nothing was refetched") {
      it("does not persist anything to elasticsearch") {
        val result = ElasticsearchSearchResult[WiktionaryDefinitionEntry](
          index = index,
          fields = fields,
          result = List(),
          fetchCount = 5,
          lookupId = None,
          refetched = false,
          queried = true
        )

        assert(result.cacheQueries.isEmpty)
        assert(result.updateAttemptsQuery.isEmpty)
        assert(result.toIndex.isEmpty)
      }
    }

    describe("where refetching gave no result") {
      val result = ElasticsearchSearchResult[WiktionaryDefinitionEntry](
        index = index,
        fields = fields,
        result = List(),
        fetchCount = 5,
        lookupId = None,
        refetched = true,
        queried = true
      )
      val attemptsQuery =
        ElasticsearchTestUtil.lookupIndexRequestFrom(
          LookupAttempt(index, fields, 5)
        )
      it("correctly saves fetch attempts to elasticsearch") {
        assert(
          result.updateAttemptsQuery.isDefined && result.updateAttemptsQuery.get.isLeft
        )
        val updateQuery = result.updateAttemptsQuery.get.left.get
        assert(updateQuery.indices().contains("attempts"))
        assert(updateQuery.sourceAsMap() == attemptsQuery.sourceAsMap())
      }
      it("does not cache anything") {
        assert(result.cacheQueries.isEmpty)
        assert(result.toIndex.isDefined && result.toIndex.get.length == 1)
      }
    }

    val dummyChineseDefinition = WiktionaryDefinitionEntry(
      subdefinitions = List("definition 1", "definition 2"),
      pronunciation = "ni hao",
      tag = Some(PartOfSpeech.NOUN),
      examples = Some(List("example 1", "example 2")),
      definitionLanguage = Language.ENGLISH,
      wordLanguage = Language.CHINESE,
      token = "你好",
      source = DefinitionSource.MULTIPLE
    )
    val dummyEnglishDefinition = WiktionaryDefinitionEntry(
      subdefinitions = List("definition 1", "definition 2"),
      pronunciation = "ipa",
      tag = Some(PartOfSpeech.NOUN),
      examples = Some(List("example 1", "example 2")),
      definitionLanguage = Language.ENGLISH,
      wordLanguage = Language.ENGLISH,
      token = "anything",
      source = DefinitionSource.MULTIPLE
    )

    implicit val writes: Writes[EnglishDefinition] =
      Json.writes[EnglishDefinition]

    val chineseDefinitionRequest =
      ElasticsearchTestUtil.indexRequestFrom(index, dummyChineseDefinition)
    val genericDefinitionRequest =
      ElasticsearchTestUtil.indexRequestFrom(index, dummyEnglishDefinition)

    describe("on a previously untried query") {
      val result = ElasticsearchSearchResult[WiktionaryDefinitionEntry](
        index = index,
        fields = fields,
        result = List(dummyChineseDefinition, dummyEnglishDefinition),
        fetchCount = 1,
        lookupId = None,
        refetched = true,
        queried = true
      )
      val attemptsQuery =
        ElasticsearchTestUtil.lookupIndexRequestFrom(
          LookupAttempt(index, fields, 1)
        )

      it("creates a new fetch attempt in elasticsearch") {
        assert(result.updateAttemptsQuery.isDefined)
        assert(result.updateAttemptsQuery.get.isLeft)

        val fetchAttempt: IndexRequest = result.updateAttemptsQuery.get.left.get
        assert(fetchAttempt.indices().contains("attempts"))
        assert(fetchAttempt.sourceAsMap() == attemptsQuery.sourceAsMap())
      }

      it("caches search results to elasticsearch") {
        assert(result.cacheQueries.isDefined)
        val cacheQueriesEither = result.cacheQueries.get

        assert(cacheQueriesEither.forall(_.isLeft))
        val cacheQueries = cacheQueriesEither.map(_.left.get)

        assert(cacheQueries.forall(_.indices().contains(index)))
        assert(
          cacheQueries
            .exists(_.sourceAsMap() == chineseDefinitionRequest.sourceAsMap())
        )
        assert(
          cacheQueries
            .exists(_.sourceAsMap() == genericDefinitionRequest.sourceAsMap())
        )

        val indexQueries: Seq[IndexRequest] =
          result.toIndex.get.flatMap(a => a.requests).map(_.left.get)
        assert(indexQueries.length == 3)
        assert(
          indexQueries.exists(_.sourceAsMap() == attemptsQuery.sourceAsMap())
        )
        assert(
          indexQueries
            .exists(_.sourceAsMap() == chineseDefinitionRequest.sourceAsMap())
        )
        assert(
          indexQueries
            .exists(_.sourceAsMap() == genericDefinitionRequest.sourceAsMap())
        )
      }
    }

    describe(
      "on a query which previously failed but contained results this time"
    ) {
      val attemptsId = "2423423"
      val result = ElasticsearchSearchResult[WiktionaryDefinitionEntry](
        index = index,
        fields = fields,
        result = List(dummyChineseDefinition, dummyEnglishDefinition),
        fetchCount = 2,
        lookupId = Some(attemptsId),
        refetched = true,
        queried = true
      )
      val attemptsQuery =
        ElasticsearchTestUtil
          .lookupUpdateRequestFrom(
            attemptsId,
            LookupAttempt(index = index, fields = fields, count = 2)
          )

      it("updates the previous fetch attempt in elasticsearch") {
        assert(
          result.updateAttemptsQuery.isDefined && result.updateAttemptsQuery.get.isRight
        )
        val updateQuery = result.updateAttemptsQuery.get.right.get
        assert(updateQuery.indices().contains("attempts"))
        assert(
          updateQuery.upsertRequest().sourceAsMap() == attemptsQuery
            .upsertRequest()
            .sourceAsMap()
        )
      }
      it("caches search results to elasticsearch") {
        assert(result.cacheQueries.isDefined)
        val cacheQueriesEither = result.cacheQueries.get

        assert(cacheQueriesEither.forall(_.isLeft))
        val cacheQueries = cacheQueriesEither.map(_.left.get)

        assert(cacheQueries.forall(_.indices().contains(index)))
        assert(
          cacheQueries
            .exists(_.sourceAsMap() == chineseDefinitionRequest.sourceAsMap())
        )
        assert(
          cacheQueries
            .exists(_.sourceAsMap() == genericDefinitionRequest.sourceAsMap())
        )

        val (indexQueries, updateQueries) = {
          val (i, u) = result.toIndex.get
            .flatMap(a => a.requests)
            .partition(_.isLeft)
          (i.map(_.left.get), u.map(_.right.get))
        }
        assert(indexQueries.length == 2)
        assert(
          indexQueries
            .exists(_.sourceAsMap() == chineseDefinitionRequest.sourceAsMap())
        )
        assert(
          indexQueries
            .exists(_.sourceAsMap() == genericDefinitionRequest.sourceAsMap())
        )

        assert(updateQueries.length == 1)
        assert(
          updateQueries.head.upsertRequest().sourceAsMap() == attemptsQuery
            .upsertRequest()
            .sourceAsMap()
        )

      }
    }
  }
}
