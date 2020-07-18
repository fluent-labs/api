package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchRequest
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

import scala.concurrent.{ExecutionContext, Future}
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
    subdefinitions = List("definition 1", "definition 2"),
    tag = Some(PartOfSpeech.NOUN),
    examples = Some(List("example 1", "example 2")),
    inputPinyin = "ni3 hao3",
    isTraditional = false,
    inputSimplified = Some("你好"),
    inputTraditional = Some("你好"),
    definitionLanguage = Language.ENGLISH,
    source = DefinitionSource.MULTIPLE,
    token = "你好"
  )

  val dummyCedictDefinition: ChineseDefinition = ChineseDefinition(
    subdefinitions = List("cedict definition 1", "cedict definition 2"),
    tag = None,
    examples = None,
    inputPinyin = "ni3 hao3",
    isTraditional = false,
    inputSimplified = Some("你好"),
    inputTraditional = Some("你好"),
    definitionLanguage = Language.ENGLISH,
    source = DefinitionSource.CEDICT,
    token = "你好"
  )

  val dummyWiktionaryDefinition = ChineseDefinition(
    subdefinitions = List("wiktionary definition 1", "wiktionary definition 2"),
    tag = Some(PartOfSpeech.NOUN),
    examples = Some(List("example 1", "example 2")),
    inputPinyin = "",
    isTraditional = false,
    inputSimplified = None,
    inputTraditional = None,
    definitionLanguage = Language.ENGLISH,
    source = DefinitionSource.WIKTIONARY,
    token = "你好"
  )

  val dummyWiktionaryDefinitionTwo = ChineseDefinition(
    subdefinitions = List("wiktionary definition 3", "wiktionary definition 4"),
    tag = Some(PartOfSpeech.NOUN),
    examples = Some(List("example 3", "example 4")),
    inputPinyin = "",
    isTraditional = false,
    inputTraditional = Some(""),
    inputSimplified = Some(""),
    definitionLanguage = Language.ENGLISH,
    source = DefinitionSource.WIKTIONARY,
    token = "你好"
  )

  describe("When getting definitions for a single word") {
    it("Does not enhance non-chinese definitions") {
      // This will delegate to the base LanguageDefinitionService implementation
      // So the assertions may fail if that changes.
      when(
        elasticsearchClientMock
          .getFromCache[Definition](
            any(classOf[Seq[ElasticsearchRequest[Definition]]])
          )(
            any(classOf[Indexable[Definition]]),
            any(classOf[HitReader[Definition]]),
            any(classOf[ClassTag[Definition]])
          )
      ).thenReturn(
        Future.successful(
          List(
            Some(List(dummyCedictDefinition)),
            Some(List(dummyWiktionaryDefinition))
          )
        )
      )

      chineseDefinitionService
        .getDefinitions(Language.CHINESE, "你好")
        .map { result =>
          assert(result.isDefined)
          val definitions = result.get
          assert(definitions.size == 2)
          assert(definitions.exists(_.eq(dummyCedictDefinition)))
          assert(definitions.exists(_.eq(dummyWiktionaryDefinition)))
        }
    }

    describe("can enhance chinese definitions") {
      it("and returns cedict definitions if no wiktionary are found") {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any(classOf[Indexable[Definition]]),
              any(classOf[HitReader[Definition]]),
              any(classOf[ClassTag[Definition]])
            )
        ).thenReturn(
          Future.successful(List(Some(List(dummyCedictDefinition)), None))
        )

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, "你好")
          .map { result =>
            assert(result.isDefined)
            val definitions = result.get
            assert(definitions.size == 1)
            assert(definitions.exists(_.eq(dummyCedictDefinition)))
          }
      }

      it(
        "and returns wiktionary definitions if no cedict definitions are found"
      ) {
        when(
          elasticsearchClientMock
            .getFromCache(any(classOf[Seq[ElasticsearchRequest[Definition]]]))(
              any(classOf[Indexable[Definition]]),
              any(classOf[HitReader[Definition]]),
              any(classOf[ClassTag[Definition]])
            )
        ).thenReturn(
          Future.successful(List(None, Some(List(dummyWiktionaryDefinition))))
        )

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, "你好")
          .map { result =>
            assert(result.isDefined)
            val definitions = result.get
            assert(definitions.size == 1)
            assert(definitions.exists(_.eq(dummyWiktionaryDefinition)))
          }
      }

      it("combines cedict and wiktionary definitions correctly") {
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
              Some(List(dummyCedictDefinition)),
              Some(
                List(dummyWiktionaryDefinition, dummyWiktionaryDefinitionTwo)
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
                combined.examples.contains(
                  (dummyWiktionaryDefinition.examples ++ dummyWiktionaryDefinitionTwo.examples).flatten
                )
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
              any(classOf[Indexable[Definition]]),
              any(classOf[HitReader[Definition]]),
              any(classOf[ClassTag[Definition]])
            )
        ).thenReturn(
          Future.successful(
            List(
              Some(List(dummyCedictDefinition.copy(subdefinitions = List()))),
              Some(
                List(dummyWiktionaryDefinition, dummyWiktionaryDefinitionTwo)
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
