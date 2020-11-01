package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.{
  ChineseDefinition,
  Definition,
  DefinitionSource,
  GenericDefinition
}
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.sksamuel.elastic4s.ElasticDsl.{indexInto, updateById}
import com.sksamuel.elastic4s.playjson._
import org.scalatest.funspec.AnyFunSpec

class ElasticsearchSearchResultTest extends AnyFunSpec {
  val index: String = "definition"
  val fields: Map[String, String] =
    Map("field1" -> "value1", "field2" -> "value2", "field3" -> "value3")
  describe("an elasticsearch result") {
    describe("where nothing was refetched") {
      it("does not persist anything to elasticsearch") {
        val result = ElasticsearchSearchResult[Definition](
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
      val result = ElasticsearchSearchResult[Definition](
        index = index,
        fields = fields,
        result = List(),
        fetchCount = 5,
        lookupId = None,
        refetched = true,
        queried = true
      )
      val attemptsQuery =
        indexInto("attempts").doc(LookupAttempt(index, fields, 5))
      it("correctly saves fetch attempts to elasticsearch") {
        assert(result.updateAttemptsQuery.contains(attemptsQuery))
      }
      it("does not cache anything") {
        assert(result.cacheQueries.isEmpty)
        assert(
          result.toIndex
            .contains(List(ElasticsearchCacheRequest(List(attemptsQuery))))
        )
      }
    }

    val dummyChineseDefinition = ChineseDefinition(
      subdefinitions = List("definition 1", "definition 2"),
      tag = PartOfSpeech.NOUN,
      examples = Some(List("example 1", "example 2")),
      inputPinyin = "ni3 hao3",
      inputSimplified = Some("你好"),
      inputTraditional = Some("你好"),
      definitionLanguage = Language.ENGLISH,
      source = DefinitionSource.MULTIPLE,
      token = "你好"
    )
    val dummyGenericDefinition = GenericDefinition(
      subdefinitions = List("definition 1", "definition 2"),
      ipa = "ipa",
      tag = PartOfSpeech.NOUN,
      examples = Some(List("example 1", "example 2")),
      definitionLanguage = Language.ENGLISH,
      wordLanguage = Language.ENGLISH,
      source = DefinitionSource.MULTIPLE,
      token = "anything"
    )

    describe("on a previously untried query") {
      val result = ElasticsearchSearchResult[Definition](
        index = index,
        fields = fields,
        result = List(dummyChineseDefinition, dummyGenericDefinition),
        fetchCount = 1,
        lookupId = None,
        refetched = true,
        queried = true
      )
      val attemptsQuery =
        indexInto("attempts").doc(LookupAttempt(index, fields, 1))
      val indexQuery = List(
        indexInto(index).doc(dummyChineseDefinition),
        indexInto(index).doc(dummyGenericDefinition)
      )
      it("creates a new fetch attempt in elasticsearch") {
        assert(result.updateAttemptsQuery.contains(attemptsQuery))
      }
      it("caches search results to elasticsearch") {
        assert(result.cacheQueries.contains(indexQuery))
        assert(
          result.toIndex
            .contains(
              List(ElasticsearchCacheRequest(attemptsQuery :: indexQuery))
            )
        )
      }
    }

    describe(
      "on a query which previously failed but contained results this time"
    ) {
      val attemptsId = "2423423"
      val result = ElasticsearchSearchResult[Definition](
        index = index,
        fields = fields,
        result = List(dummyChineseDefinition, dummyGenericDefinition),
        fetchCount = 2,
        lookupId = Some(attemptsId),
        refetched = true,
        queried = true
      )
      val attemptsQuery =
        updateById("attempts", attemptsId)
          .doc(LookupAttempt(index = index, fields = fields, count = 2))

      val indexQuery = List(
        indexInto(index).doc(dummyChineseDefinition),
        indexInto(index).doc(dummyGenericDefinition)
      )
      it("updates the previous fetch attempt in elasticsearch") {
        assert(result.updateAttemptsQuery.contains(attemptsQuery))
      }
      it("caches search results to elasticsearch") {
        assert(result.cacheQueries.contains(indexQuery))
        assert(
          result.toIndex
            .contains(
              List(ElasticsearchCacheRequest(attemptsQuery :: indexQuery))
            )
        )
      }
    }
  }
}
