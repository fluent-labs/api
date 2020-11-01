package com.foreignlanguagereader.api.service.definition

import cats.data.Nested
import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.common.CircuitBreakerResult
import com.foreignlanguagereader.api.client.elasticsearch.{
  ElasticsearchClient,
  LookupAttempt
}
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchSearchRequest
import com.foreignlanguagereader.domain.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.external.definition.WiktionaryDefinitionEntry
import com.foreignlanguagereader.domain.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.domain.internal.word.{PartOfSpeech, Word}
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
    token = "test"
  )
  val elasticsearchClientMock: ElasticsearchClient = mock[ElasticsearchClient]
  val languageServiceClientMock: LanguageServiceClient =
    mock[LanguageServiceClient]

  val test: Word = Word.fromToken("test", Language.ENGLISH)
  val token: Word = Word.fromToken("token", Language.ENGLISH)

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
          .findFromCacheOrRefetch(
            any(classOf[List[ElasticsearchSearchRequest[Definition]]])
          )(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[HitReader[LookupAttempt]]),
            any(classOf[ClassTag[Definition]])
          )
      ).thenReturn(
        Future
          .successful(
            List(
              List(dummyWiktionaryDefinition.toDefinition(PartOfSpeech.NOUN))
            )
          )
      )
      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, test)
        .map { results =>
          assert(results.length == 1)
          assert(
            results(0) == dummyWiktionaryDefinition
              .toDefinition(PartOfSpeech.NOUN)
          )
        }
    }

    it("does not break if no results are found") {
      when(
        elasticsearchClientMock
          .findFromCacheOrRefetch(
            any(classOf[List[ElasticsearchSearchRequest[Definition]]])
          )(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[HitReader[LookupAttempt]]),
            any(classOf[ClassTag[Definition]])
          )
      ).thenReturn(Future.successful(List(List())))

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, test)
        .map { response =>
          assert(response.isEmpty)
        }
    }

    it("does not break if a future is failed") {
      when(
        elasticsearchClientMock
          .findFromCacheOrRefetch(
            any(classOf[List[ElasticsearchSearchRequest[Definition]]])
          )(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[HitReader[LookupAttempt]]),
            any(classOf[ClassTag[Definition]])
          )
      ).thenReturn(Future.successful(List(List())))
      when(
        languageServiceClientMock.getDefinition(
          Language.ENGLISH,
          Word.fromToken("test", Language.ENGLISH)
        )
      ).thenReturn(
        Nested(
          Future.failed[CircuitBreakerResult[List[Definition]]](
            new IllegalStateException("Uh oh")
          )
        )
      )

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, test)
        .map { response =>
          assert(response.isEmpty)
        }
    }

    it("does not break if a fetcher cannot work with the requested language") {
      when(
        elasticsearchClientMock
          .findFromCacheOrRefetch(
            any(classOf[List[ElasticsearchSearchRequest[Definition]]])
          )(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[HitReader[LookupAttempt]]),
            any(classOf[ClassTag[Definition]])
          )
      ).thenReturn(Future.successful(List(List())))

      defaultDefinitionService
        .getDefinitions(Language.CHINESE, test)
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
            word: Word,
            definitions: Map[DefinitionSource, List[Definition]]
        ): List[Definition] = {
          val stub: Map[DefinitionSource, List[Definition]] = Map(
            DefinitionSource.CEDICT ->
              List(dummyCEDICTDefinition.toDefinition(PartOfSpeech.NOUN)),
            DefinitionSource.WIKTIONARY ->
              List(dummyWiktionaryDefinition.toDefinition(PartOfSpeech.NOUN))
          )
          (definitionLanguage, word, definitions) match {
            case (Language.ENGLISH, token, stub) =>
              List(
                dummyWiktionaryDefinition.toDefinition(PartOfSpeech.NOUN),
                dummyCEDICTDefinition.toDefinition(PartOfSpeech.NOUN)
              )
            case _ => List()
          }
        }
      }
      val customizedEnricher = new CustomizedEnricherLangaugeDefinitionService()

      it("can define how to enrich definitions") {
        when(
          elasticsearchClientMock
            .findFromCacheOrRefetch(
              any(classOf[List[ElasticsearchSearchRequest[Definition]]])
            )(
              any(classOf[Indexable[Definition]]),
              any(classOf[HitReader[Definition]]),
              any(classOf[HitReader[LookupAttempt]]),
              any(classOf[ClassTag[Definition]])
            )
        ).thenReturn(
          Future.successful(
            List(
              List(dummyCEDICTDefinition.toDefinition(PartOfSpeech.NOUN)),
              List(dummyWiktionaryDefinition.toDefinition(PartOfSpeech.NOUN))
            )
          )
        )

        customizedEnricher
          .getDefinitions(Language.ENGLISH, token)
          .map { results =>
            assert(results.length == 2)
            assert(
              results.contains(
                dummyWiktionaryDefinition.toDefinition(PartOfSpeech.NOUN)
              )
            )
            assert(
              results
                .contains(dummyCEDICTDefinition.toDefinition(PartOfSpeech.NOUN))
            )

            succeed
          }
      }
    }
  }
}
