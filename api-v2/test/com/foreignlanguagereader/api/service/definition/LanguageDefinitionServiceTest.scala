package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchRequest
import com.foreignlanguagereader.api.contentsource.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.api.contentsource.definition.{
  DefinitionEntry,
  WiktionaryDefinitionEntry
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.word.PartOfSpeech
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class LanguageDefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val dummyWiktionaryDefinition = WiktionaryDefinitionEntry(
    List("definition 1", "definition 2"),
    Some(PartOfSpeech.NOUN),
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
      override val sources: List[DefinitionSource] =
        List(DefinitionSource.WIKTIONARY)
    }
    val defaultDefinitionService = new DefaultLanguageDefinitionService()

    it("will return results from elasticsearch if they are found") {
      when(
        elasticsearchClientMock
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
      ).thenReturn(
        Future
          .successful(List(Some(List(dummyWiktionaryDefinition.toDefinition))))
      )
      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, "test")
        .map { response =>
          assert(response.isDefined)
          val results = response.get
          assert(results.length == 1)
          assert(results(0) == dummyWiktionaryDefinition.toDefinition)
        }
    }

    it("does not break if no results are found") {
      when(
        elasticsearchClientMock
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
      ).thenReturn(Future.successful(List(None)))
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
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
      ).thenReturn(Future.successful(List(None)))
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
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
      ).thenReturn(Future.successful(List(None)))

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
      override val sources: List[DefinitionSource] =
        List(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)
    }

    describe("with a custom fetcher") {
      class CustomizedFetcherLanguageDefinitionService
          extends CustomizedLanguageDefinitionService {
        override val definitionFetchers
          : Map[(DefinitionSource, Language),
                (Language, String) => Future[Option[Seq[DefinitionEntry]]]] =
          Map(
            (DefinitionSource.CEDICT, Language.ENGLISH) -> testFetcher,
            (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
          )

        def testFetcher
          : (Language, String) => Future[Option[Seq[DefinitionEntry]]] =
          (language: Language, word: String) =>
            languageServiceClientMock.getDefinition(language, word)
      }

      val customizedFetcher = new CustomizedFetcherLanguageDefinitionService()

      it("will refetch from web sources that were not found in elasticsearch") {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
        ).thenReturn(
          Future.successful(
            List(None, Some(List(dummyWiktionaryDefinition.toDefinition)))
          )
        )
        when(languageServiceClientMock.getDefinition(Language.ENGLISH, "token"))
          .thenReturn(Future.successful(Some(List(dummyCEDICTDefinition))))

        customizedFetcher
          .getDefinitions(Language.ENGLISH, "token")
          .map { response =>
            assert(response.isDefined)
            val results = response.get
            assert(results.length == 2)
            results.find(_ == dummyWiktionaryDefinition.toDefinition)
            results.find(_ == dummyCEDICTDefinition.toDefinition)

            succeed
          }
      }

      it("does not break if refetching does not return results") {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
        ).thenReturn(Future.successful(List(None, None)))
        when(languageServiceClientMock.getDefinition(Language.ENGLISH, "token"))
          .thenReturn(Future.successful(Some(List(dummyWiktionaryDefinition))))

        customizedFetcher
          .getDefinitions(Language.ENGLISH, "token")
          .map { response =>
            assert(response.isDefined)
            val results = response.get
            assert(results.length == 1)
            results.find(_ == dummyWiktionaryDefinition.toDefinition)

            succeed
          }
      }
    }

    describe("with a custom enricher") {
      class CustomizedEnricherLangaugeDefinitionService
          extends CustomizedLanguageDefinitionService {
        override def enrichDefinitions(
          definitionLanguage: Language,
          word: String,
          definitions: Map[DefinitionSource, Option[Seq[Definition]]]
        ): Seq[Definition] = {
          val stub: Map[DefinitionSource, Option[Seq[Definition]]] = Map(
            DefinitionSource.CEDICT -> Some(
              List(dummyCEDICTDefinition.toDefinition)
            ),
            DefinitionSource.WIKTIONARY -> Some(
              List(dummyWiktionaryDefinition.toDefinition)
            )
          )
          (definitionLanguage, word, definitions) match {
            case (Language.ENGLISH, "token", stub) =>
              List(
                dummyWiktionaryDefinition.toDefinition,
                dummyCEDICTDefinition.toDefinition
              )
            case _ => List()
          }
        }
      }
      val customizedEnricher = new CustomizedEnricherLangaugeDefinitionService()

      it("can define how to enrich definitions") {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))
        ).thenReturn(
          Future.successful(
            List(
              Some(List(dummyCEDICTDefinition.toDefinition)),
              Some(List(dummyWiktionaryDefinition.toDefinition))
            )
          )
        )

        customizedEnricher
          .getDefinitions(Language.ENGLISH, "token")
          .map { response =>
            assert(response.isDefined)
            val results = response.get
            assert(results.length == 2)
            assert(results.exists(_.eq(dummyWiktionaryDefinition.toDefinition)))
            assert(results.exists(_.eq(dummyCEDICTDefinition.toDefinition)))

            succeed
          }
      }
    }
  }
}
