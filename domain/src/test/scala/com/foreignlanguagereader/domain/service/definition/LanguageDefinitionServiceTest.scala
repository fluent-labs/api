package com.foreignlanguagereader.domain.service.definition

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.webster.WebsterSpanishDefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource,
  EnglishDefinition,
  SpanishDefinition
}
import com.foreignlanguagereader.content.types.internal.word.{
  PartOfSpeech,
  Word
}
import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.common.CircuitBreakerResult
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchCacheClient
import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.ElasticsearchSearchRequest
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.fetcher.spanish.WebsterSpanishToEnglishFetcher
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.FutureOutcome
import org.scalatest.funspec.AsyncFunSpec
import play.api.Configuration
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class LanguageDefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val dummyWiktionaryDefinition: WiktionaryDefinitionEntry =
    WiktionaryDefinitionEntry(
      subdefinitions = List("definition 1", "definition 2"),
      pronunciation = "",
      tag = Some(PartOfSpeech.NOUN),
      examples = Some(List("example 1", "example 2")),
      definitionLanguage = Language.ENGLISH,
      wordLanguage = Language.ENGLISH,
      token = "test"
    )
  val elasticsearchClientMock: ElasticsearchCacheClient =
    mock[ElasticsearchCacheClient]
  val configMock: Configuration = mock[Configuration]

  val test: Word = Word.fromToken("test", Language.ENGLISH)
  val token: Word = Word.fromToken("token", Language.ENGLISH)

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    when(configMock.get[String]("environment")).thenReturn("test")

    complete {
      super.withFixture(test) // Invoke the test function
    } lastly {}
  }

  describe("A default language definition service") {
    class DefaultLanguageDefinitionService()
        extends LanguageDefinitionService[EnglishDefinition] {
      val elasticsearch: ElasticsearchCacheClient = elasticsearchClientMock
      implicit val ec: ExecutionContext =
        scala.concurrent.ExecutionContext.Implicits.global
      override val config: Configuration = configMock
      override val wordLanguage: Language = Language.ENGLISH
      override val sources: List[DefinitionSource] =
        List(DefinitionSource.WIKTIONARY)
    }
    val defaultDefinitionService = new DefaultLanguageDefinitionService()

    it("will return results from elasticsearch if they are found") {
      when(
        elasticsearchClientMock
          .findFromCacheOrRefetch(
            any(classOf[ElasticsearchSearchRequest[WiktionaryDefinitionEntry]])
          )(
            any(classOf[ClassTag[WiktionaryDefinitionEntry]]),
            any(classOf[Reads[WiktionaryDefinitionEntry]]),
            any(classOf[Writes[WiktionaryDefinitionEntry]])
          )
      ).thenReturn(
        Future
          .successful(
            List(dummyWiktionaryDefinition)
          )
      )
      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, test)
        .map { results =>
          assert(results.length == 1)
          assert(
            results.head == dummyWiktionaryDefinition
              .toDefinition(PartOfSpeech.NOUN)
          )
        }
    }

    it("does not break if no results are found") {
      when(
        elasticsearchClientMock
          .findFromCacheOrRefetch(
            any(classOf[ElasticsearchSearchRequest[WiktionaryDefinitionEntry]])
          )(
            any(classOf[ClassTag[WiktionaryDefinitionEntry]]),
            any(classOf[Reads[WiktionaryDefinitionEntry]]),
            any(classOf[Writes[WiktionaryDefinitionEntry]])
          )
      ).thenReturn(Future.successful(List()))

      defaultDefinitionService
        .getDefinitions(Language.ENGLISH, test)
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
        extends LanguageDefinitionService[SpanishDefinition] {
      val elasticsearch: ElasticsearchCacheClient = elasticsearchClientMock
      implicit val ec: ExecutionContext =
        scala.concurrent.ExecutionContext.Implicits.global
      override val config: Configuration = configMock
      override val wordLanguage: Language = Language.SPANISH
      override val sources: List[DefinitionSource] =
        List(
          DefinitionSource.MIRRIAM_WEBSTER_SPANISH
        )
    }

    describe("with a custom fetcher") {
      val websterMock: MirriamWebsterClient =
        mock[MirriamWebsterClient]

      class CustomizedFetcherLanguageDefinitionService
          extends CustomizedLanguageDefinitionService {

        override val definitionFetchers: Map[
          (DefinitionSource, Language),
          DefinitionFetcher[_, SpanishDefinition]
        ] = Map(
          (
            DefinitionSource.MIRRIAM_WEBSTER_SPANISH,
            Language.SPANISH
          ) -> new WebsterSpanishToEnglishFetcher(websterMock)(ec)
        )
      }
      val customizedFetcher = new CustomizedFetcherLanguageDefinitionService()

      it("does not break if a future is failed") {
        when(
          elasticsearchClientMock
            .findFromCacheOrRefetch(
              any(
                classOf[
                  ElasticsearchSearchRequest[WebsterSpanishDefinitionEntry]
                ]
              )
            )(
              any(classOf[ClassTag[WebsterSpanishDefinitionEntry]]),
              any(classOf[Reads[WebsterSpanishDefinitionEntry]]),
              any(classOf[Writes[WebsterSpanishDefinitionEntry]])
            )
        ).thenReturn(Future.successful(List()))
        when(
          websterMock.getSpanishDefinition(
            Word.fromToken("test", Language.SPANISH)
          )
        ).thenReturn(
          Future
            .failed[CircuitBreakerResult[List[WebsterSpanishDefinitionEntry]]](
              new IllegalStateException("Uh oh")
            )
        )

        customizedFetcher
          .getDefinitions(Language.SPANISH, test)
          .map { response =>
            assert(response.isEmpty)
          }
      }

      it(
        "does not break if a fetcher cannot work with the requested language"
      ) {
        when(
          elasticsearchClientMock
            .findFromCacheOrRefetch(
              any(classOf[List[ElasticsearchSearchRequest[Definition]]])
            )(
              any(classOf[ClassTag[Definition]]),
              any(classOf[Reads[Definition]]),
              any(classOf[Writes[Definition]])
            )
        ).thenReturn(Future.successful(List(List())))

        customizedFetcher
          .getDefinitions(Language.CHINESE, test)
          .map { response =>
            assert(response.isEmpty)
          }
      }
    }

    describe("with a custom enricher") {
      class CustomizedEnricherLanguageDefinitionService
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
      val customizedEnricher = new CustomizedEnricherLanguageDefinitionService()

      it("can define how to enrich definitions") {
        when(
          elasticsearchClientMock
            .findFromCacheOrRefetch(
              any(classOf[List[ElasticsearchSearchRequest[Definition]]])
            )(
              any(classOf[ClassTag[Definition]]),
              any(classOf[Reads[Definition]]),
              any(classOf[Writes[Definition]])
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
