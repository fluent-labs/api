package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchRequest
import com.foreignlanguagereader.api.contentsource.definition.WiktionaryDefinitionEntry
import com.foreignlanguagereader.api.contentsource.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.word.PartOfSpeech
import com.sksamuel.elastic4s.{HitReader, Indexable}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class LanguageDefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val dummyWiktionaryDefinition = WiktionaryDefinitionEntry(
    subdefinitions = List("definition 1", "definition 2"),
    pronunciation = "",
    tag = Some(PartOfSpeech.NOUN),
    examples = Some(List("example 1", "example 2")),
    definitionLanguage = Language.ENGLISH,
    wordLanguage = Language.ENGLISH,
    token = "test",
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
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[ClassTag[Definition]])
          )
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
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[ClassTag[Definition]])
          )
      ).thenReturn(Future.successful(List(None)))

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, "test")
        .map { response =>
          assert(response.isEmpty)
        }
    }

    it("does not break if a future is failed") {
      when(
        elasticsearchClientMock
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[ClassTag[Definition]])
          )
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
          .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[ClassTag[Definition]])
          )
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
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any(classOf[Indexable[Definition]]),
              any(classOf[HitReader[Definition]]),
              any(classOf[ClassTag[Definition]])
            )
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
