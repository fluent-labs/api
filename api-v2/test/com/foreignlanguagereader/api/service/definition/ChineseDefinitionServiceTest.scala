package com.foreignlanguagereader.api.service.definition

import java.util.concurrent.TimeUnit

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.combined.ChineseDefinition
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionSource,
  WiktionaryDefinitionEntry
}
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ChineseDefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val elasticsearchClientMock: ElasticsearchClient = mock[ElasticsearchClient]
  val languageServiceClientMock: LanguageServiceClient =
    mock[LanguageServiceClient]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val chineseDefinitionService = new ChineseDefinitionService(
    elasticsearchClientMock,
    languageServiceClientMock,
    ec
  )

  val dummyChineseDefinition = ChineseDefinition(
    List("definition 1", "definition 2"),
    "noun",
    List("example 1", "example 2"),
    "ni3 hao3",
    "你好",
    "你好",
    Language.ENGLISH,
    DefinitionSource.MULTIPLE,
    token = "你好"
  )

  val dummyCedictDefinition = CEDICTDefinitionEntry(
    List("cedict definition 1", "cedict definition 2"),
    "ni3 hao3",
    "你好",
    "你好",
    "你好"
  )

  val dummyWiktionaryDefinition = WiktionaryDefinitionEntry(
    List("wiktionary definition 1", "wiktionary definition 2"),
    "noun",
    List("example 1", "example 2"),
    Language.CHINESE,
    Language.ENGLISH,
    "你好"
  )

  describe("When getting definitions for a single word") {
    it("Does not enhance non-english definitions") {
      // This will delegate to the base LanguageDefinitionService implementation
      // So the assertions may fail if that changes.

      when(
        elasticsearchClientMock
          .getDefinition(Language.CHINESE, Language.CHINESE, "你好")
      ).thenReturn(Some(List(dummyCedictDefinition, dummyWiktionaryDefinition)))

      chineseDefinitionService
        .getDefinitions(Language.CHINESE, "你好")
        .map { result =>
          assert(result.isDefined)
          val definitions = result.get
          assert(definitions.size == 2)
          assert(definitions.exists(_.eq(dummyCedictDefinition.toDefinition)))
          assert(
            definitions.exists(_.eq(dummyWiktionaryDefinition.toDefinition))
          )
        }
    }
    describe("it can enhance english definitions") {
      it("and throws an error if no definitions are given to it") {
        when(
          elasticsearchClientMock
            .getDefinition(Language.CHINESE, Language.ENGLISH, "你好")
        ).thenReturn(Some(List()))
        when(languageServiceClientMock.getDefinition(Language.CHINESE, "你好"))
          .thenReturn(Future.successful(Some(List())))

        assertThrows[IllegalStateException] {
          Await.result(
            chineseDefinitionService
              .getDefinitions(Language.ENGLISH, "你好"),
            Duration(5, TimeUnit.SECONDS)
          )
        }
      }

      it("and returns cedict definitions if no wiktionary are found") {
        when(
          elasticsearchClientMock
            .getDefinition(Language.CHINESE, Language.ENGLISH, "你好")
        ).thenReturn(Some(List()))
        when(languageServiceClientMock.getDefinition(Language.CHINESE, "你好"))
          .thenReturn(Future.successful(Some(List(dummyCedictDefinition))))

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, "你好")
          .map { result =>
            assert(result.isDefined)
            val definitions = result.get
            assert(definitions.size == 1)
            assert(definitions.exists(_.eq(dummyCedictDefinition.toDefinition)))
          }
      }

      it(
        "and returns wiktionary definitions if no cedict definitions are found"
      ) {
        when(
          elasticsearchClientMock
            .getDefinition(Language.CHINESE, Language.ENGLISH, "你好")
        ).thenReturn(Some(List()))
        when(languageServiceClientMock.getDefinition(Language.CHINESE, "你好"))
          .thenReturn(Future.successful(Some(List(dummyWiktionaryDefinition))))

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, "你好")
          .map { result =>
            assert(result.isDefined)
            val definitions = result.get
            assert(definitions.size == 1)
            assert(
              definitions.exists(_.eq(dummyWiktionaryDefinition.toDefinition))
            )
          }
      }
    }
  }
}
