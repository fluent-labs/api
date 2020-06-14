package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionEntry,
  DefinitionSource,
  WiktionaryDefinitionEntry
}
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class LanguageDefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val dummyWiktionaryDefinition = WiktionaryDefinitionEntry(
    List("definition 1", "definition 2"),
    "tag",
    List("example 1", "example 2"),
    Language.ENGLISH,
    Language.ENGLISH,
    "test",
  )
  val elasticsearchClientMock: ElasticsearchClient = mock[ElasticsearchClient]
  val languageServiceClientMock: LanguageServiceClient =
    mock[LanguageServiceClient]

  describe("A default language definition service") {
    class DefaultLanguageDefinitionService() extends LanguageDefinitionService {
      val elasticsearch: ElasticsearchClient = elasticsearchClientMock
      override val languageServiceClient: LanguageServiceClient =
        languageServiceClientMock
      implicit val ec: ExecutionContext =
        scala.concurrent.ExecutionContext.Implicits.global
      override val wordLanguage: Language = Language.ENGLISH
      override val sources: Set[DefinitionSource] =
        Set(DefinitionSource.WIKTIONARY)
      override val webSources: Set[DefinitionSource] =
        Set(DefinitionSource.WIKTIONARY)
    }
    val defaultDefinitionService = new DefaultLanguageDefinitionService()

    it("will return results from elasticsearch if they are found") {
      when(
        elasticsearchClientMock
          .getDefinition(Language.ENGLISH, Language.ENGLISH, "test")
      ).thenReturn(Some(List(dummyWiktionaryDefinition)))
      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, "test")
        .map { response =>
          assert(response.isDefined)
          val results = response.get
          assert(results.length == 1)
          assert(results(0) == dummyWiktionaryDefinition.toDefinition)
        }
    }

    it(
      "will return results from language service if elasticsearch cannot find definitions"
    ) {
      when(
        elasticsearchClientMock
          .getDefinition(Language.ENGLISH, Language.ENGLISH, "test")
      ).thenReturn(None)
      when(languageServiceClientMock.getDefinition(Language.ENGLISH, "test"))
        .thenReturn(Future.successful(Some(List(dummyWiktionaryDefinition))))

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, "test")
        .map { response =>
          assert(response.isDefined)
          val results = response.get
          assert(results.length == 1)
          assert(results(0) == dummyWiktionaryDefinition.toDefinition)

          verify(elasticsearchClientMock, times(1))
            .saveDefinitions(List(dummyWiktionaryDefinition))

          succeed
        }
    }

    it("does not break if no results are found") {
      when(
        elasticsearchClientMock
          .getDefinition(Language.ENGLISH, Language.ENGLISH, "test")
      ).thenReturn(None)
      when(languageServiceClientMock.getDefinition(Language.ENGLISH, "test"))
        .thenReturn(Future.successful(None))

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, "test")
        .map { response =>
          assert(response.isEmpty)
        }
    }

    it("does not break if a future is failed") {
      when(
        elasticsearchClientMock
          .getDefinition(Language.ENGLISH, Language.ENGLISH, "test")
      ).thenReturn(None)
      when(languageServiceClientMock.getDefinition(Language.ENGLISH, "test"))
        .thenReturn(Future.failed(new IllegalStateException("Uh oh")))

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, "test")
        .map { response =>
          assert(response.isEmpty)
        }
    }

    it("does not break if a fetcher cannot work with the requested language") {
      when(
        elasticsearchClientMock
          .getDefinition(Language.ENGLISH, Language.CHINESE, "test")
      ).thenReturn(None)

      defaultDefinitionService
        .getDefinitions(Language.CHINESE, "test")
        .map { response =>
          assert(response.isEmpty)
        }
    }
  }

  describe("A customized language definition service") {
    val dummyCEDICTDefinition = CEDICTDefinitionEntry(
      List("definition 1", "definition 2"),
      "pinyin",
      "simplified",
      "traditional",
      "token"
    )

    class CustomizedLanguageDefinitionService()
        extends LanguageDefinitionService {
      val elasticsearch: ElasticsearchClient = elasticsearchClientMock
      override val languageServiceClient: LanguageServiceClient =
        languageServiceClientMock
      implicit val ec: ExecutionContext =
        scala.concurrent.ExecutionContext.Implicits.global
      override val wordLanguage: Language = Language.CHINESE
      override val sources: Set[DefinitionSource] =
        Set(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)
      override val webSources: Set[DefinitionSource] =
        Set(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)
      override val definitionFetchers
        : Map[(DefinitionSource, Language),
              (Language, String) => Future[Option[Seq[DefinitionEntry]]]] = Map(
        (DefinitionSource.CEDICT, Language.ENGLISH) -> testFetcher,
        (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
      )

      def testFetcher
        : (Language, String) => Future[Option[Seq[DefinitionEntry]]] =
        (language: Language, word: String) =>
          languageServiceClientMock.getDefinition(language, word)

      override def enrichDefinitions(
        definitionLanguage: Language,
        word: String,
        definitions: Seq[DefinitionEntry]
      ): Seq[Definition] = (definitionLanguage, word, definitions) match {
        case (
            Language.ENGLISH,
            "token",
            List(dummyWiktionaryDefinition, dummyCEDICTDefinition)
            ) =>
          List(
            dummyWiktionaryDefinition.toDefinition,
            dummyCEDICTDefinition.toDefinition
          )
        case _ => List()
      }
    }
    val customized = new CustomizedLanguageDefinitionService()
    it("will refetch from web sources that were not found in elasticsearch") {
      when(
        elasticsearchClientMock
          .getDefinition(Language.CHINESE, Language.ENGLISH, "token")
      ).thenReturn(Some(List(dummyWiktionaryDefinition)))
      when(languageServiceClientMock.getDefinition(Language.CHINESE, "token"))
        .thenReturn(Future.successful(Some(List(dummyCEDICTDefinition))))

      customized
        .getDefinitions(Language.ENGLISH, "token")
        .map { response =>
          assert(response.isDefined)
          val results = response.get
          assert(results.length == 2)
          assert(results(0) == dummyCEDICTDefinition.toDefinition)
          assert(results(1) == dummyWiktionaryDefinition.toDefinition)

          verify(elasticsearchClientMock, never())
            .saveDefinitions(List(dummyWiktionaryDefinition))

          succeed
        }
    }

    it("can define how to enrich definitions") {
      when(
        elasticsearchClientMock
          .getDefinition(Language.CHINESE, Language.ENGLISH, "token")
      ).thenReturn(Some(List(dummyCEDICTDefinition, dummyWiktionaryDefinition)))

      customized
        .getDefinitions(Language.ENGLISH, "token")
        .map { response =>
          assert(response.isDefined)
          val results = response.get
          assert(results.length == 2)
          assert(results(0) == dummyCEDICTDefinition.toDefinition)
          assert(results(1) == dummyWiktionaryDefinition.toDefinition)

          verify(elasticsearchClientMock, never())
            .saveDefinitions(List(dummyWiktionaryDefinition))

          succeed
        }
    }
  }
}
