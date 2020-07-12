package com.foreignlanguagereader.api.service.definition

import java.util.concurrent.TimeUnit

import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchRequest
import com.foreignlanguagereader.api.contentsource.definition.WiktionaryDefinitionEntry
import com.foreignlanguagereader.api.contentsource.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.{
  ChineseDefinition,
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.word.PartOfSpeech
import com.sksamuel.elastic4s.{HitReader, Indexable}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag

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
  val definitionsIndex = "definitions"

  val dummyChineseDefinition = ChineseDefinition(
    List("definition 1", "definition 2"),
    Some(PartOfSpeech.NOUN),
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
    Some(PartOfSpeech.NOUN),
    List("example 1", "example 2"),
    Language.CHINESE,
    Language.ENGLISH,
    "你好"
  )

  val dummyWiktionaryDefinitionTwo = WiktionaryDefinitionEntry(
    List("wiktionary definition 3", "wiktionary definition 4"),
    Some(PartOfSpeech.NOUN),
    List("example 3", "example 4"),
    Language.CHINESE,
    Language.ENGLISH,
    "你好"
  )

  describe("When getting definitions for a single word") {
    it("Does not enhance non-chinese definitions") {
      // This will delegate to the base LanguageDefinitionService implementation
      // So the assertions may fail if that changes.
      when(
        elasticsearchClientMock
          .getFromCache[Definition](
            any(classOf[Seq[ElasticsearchRequest[Definition]]])
          )
      ).thenReturn(
        Future.successful(
          List(
            Some(List(dummyCedictDefinition.toDefinition)),
            Some(List(dummyWiktionaryDefinition.toDefinition))
          )
        )
      )

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

    describe("can enhance chinese definitions") {
      it("and throws an error if no definitions are given to it") {
        when(
          elasticsearchClientMock
            .getFromCache(any[Seq[ElasticsearchRequest[Definition]]])(
              any[Indexable[Definition]],
              any[HitReader[Definition]],
              any[ClassTag[Definition]]
            )
        ).thenReturn(Future.successful(List(Some(List()))))
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
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any[Indexable[Definition]],
              any[HitReader[Definition]],
              any[ClassTag[Definition]]
            )
        ).thenReturn(Future.successful(List(Some(List()))))
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
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any[Indexable[Definition]],
              any[HitReader[Definition]],
              any[ClassTag[Definition]]
            )
        ).thenReturn(Future.successful(List(None)))
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

      it("combines cedict and wiktionary definitions correctly") {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any[Indexable[Definition]],
              any[HitReader[Definition]],
              any[ClassTag[Definition]]
            )
        ).thenReturn(Future.successful(List(Some(List()))))
        when(languageServiceClientMock.getDefinition(Language.CHINESE, "你好"))
          .thenReturn(
            Future.successful(
              Some(
                List(
                  dummyCedictDefinition,
                  dummyWiktionaryDefinition,
                  dummyWiktionaryDefinitionTwo
                )
              )
            )
          )

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, "你好")
          .map {
            case result: Some[Seq[ChineseDefinition]] =>
              assert(result.isDefined)
              val definitions = result.get
              assert(definitions.size == 1)
              val combined = definitions(0)
              assert(
                combined.subdefinitions == dummyCedictDefinition.subdefinitions
              )
              assert(combined.tag == dummyWiktionaryDefinition.tag)
              assert(
                combined.examples == (dummyWiktionaryDefinitionTwo.examples ++ dummyWiktionaryDefinition.examples)
              )
              assert(combined.pronunciation.pinyin == "ni hao")
              assert(combined.simplified == dummyCedictDefinition.simplified)
              assert(combined.traditional == dummyCedictDefinition.traditional)
              assert(combined.definitionLanguage == Language.ENGLISH)
              assert(combined.source == DefinitionSource.MULTIPLE)
              assert(combined.token == dummyCedictDefinition.token)
          }
      }

      it(
        "combines cedict and wiktionary definitions correctly when cedict entries are missing key data"
      ) {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any[Indexable[Definition]],
              any[HitReader[Definition]],
              any[ClassTag[Definition]]
            )
        ).thenReturn(Future.successful(List(None)))
        when(languageServiceClientMock.getDefinition(Language.CHINESE, "你好"))
          .thenReturn(
            Future.successful(
              Some(
                List(
                  dummyCedictDefinition.copy(subdefinitions = List()),
                  dummyWiktionaryDefinitionTwo,
                  dummyWiktionaryDefinition
                )
              )
            )
          )

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, "你好")
          .map {
            case result: Some[Seq[ChineseDefinition]] =>
              assert(result.isDefined)
              val definitions = result.get
              assert(definitions.size == 2)
              val combinedOne = definitions(0)
              val combinedTwo = definitions(1)

              print(combinedOne)
              print(combinedTwo)

              // Cedict sourced data should be the same for all
              assert(definitions.forall(_.pronunciation.pinyin == "ni hao"))
              assert(
                definitions
                  .forall(_.simplified == dummyCedictDefinition.simplified)
              )
              assert(
                definitions
                  .forall(_.traditional == dummyCedictDefinition.traditional)
              )
              assert(definitions.forall(_.token == dummyCedictDefinition.token))

              // Generated data should be the same for all
              assert(
                definitions.forall(_.definitionLanguage == Language.ENGLISH)
              )
              assert(definitions.forall(_.source == DefinitionSource.MULTIPLE))

              assert(
                combinedOne.subdefinitions == dummyWiktionaryDefinition.subdefinitions
              )
              assert(combinedOne.tag == dummyWiktionaryDefinition.tag)
              assert(combinedOne.examples == dummyWiktionaryDefinition.examples)
              assert(
                combinedTwo.subdefinitions == dummyWiktionaryDefinitionTwo.subdefinitions
              )
              assert(combinedTwo.tag == dummyWiktionaryDefinitionTwo.tag)
              assert(
                combinedTwo.examples == dummyWiktionaryDefinitionTwo.examples
              )
          }
      }
    }
  }
}
