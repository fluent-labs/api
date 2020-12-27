package com.foreignlanguagereader.domain.service.definition

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.{
  ChineseDefinition,
  DefinitionSource
}
import com.foreignlanguagereader.content.types.internal.word.{
  PartOfSpeech,
  Word
}
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchCacheClient
import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.ElasticsearchSearchRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec
import play.api.Configuration
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ChineseDefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val elasticsearchClientMock: ElasticsearchCacheClient =
    mock[ElasticsearchCacheClient]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val configMock: Configuration = mock[Configuration]

  val chineseDefinitionService = new ChineseDefinitionService(
    elasticsearchClientMock,
    configMock,
    ec
  )
  val definitionsIndex = "definitions"

  val dummyChineseDefinition: ChineseDefinition = ChineseDefinition(
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

  val dummyCedictDefinitionEntry: CEDICTDefinitionEntry = CEDICTDefinitionEntry(
    subdefinitions = List("cedict definition 1", "cedict definition 2"),
    pinyin = "ni3 hao3",
    simplified = "你好",
    traditional = "你好",
    token = "你好"
  )
  val dummyCedictDefinition: ChineseDefinition =
    dummyCedictDefinitionEntry.toDefinition(PartOfSpeech.NOUN)

  val dummyWiktionaryDefinitionEntry: WiktionaryDefinitionEntry =
    WiktionaryDefinitionEntry(
      subdefinitions =
        List("wiktionary definition 1", "wiktionary definition 2"),
      pronunciation = "ni hao",
      tag = Some(PartOfSpeech.NOUN),
      examples = Some(List("example 1", "example 2")),
      wordLanguage = Language.CHINESE,
      definitionLanguage = Language.ENGLISH,
      token = "你好",
      source = DefinitionSource.WIKTIONARY
    )
  val dummyWiktionaryDefinition: ChineseDefinition =
    DefinitionEntry.buildChineseDefinition(
      dummyWiktionaryDefinitionEntry,
      PartOfSpeech.NOUN
    )

  val dummyWiktionaryDefinitionEntryTwo: WiktionaryDefinitionEntry =
    WiktionaryDefinitionEntry(
      subdefinitions =
        List("wiktionary definition 3", "wiktionary definition 4"),
      pronunciation = "ni hao",
      tag = Some(PartOfSpeech.NOUN),
      examples = Some(List("example 3", "example 4")),
      wordLanguage = Language.CHINESE,
      definitionLanguage = Language.ENGLISH,
      token = "你好",
      source = DefinitionSource.WIKTIONARY
    )
  val dummyWiktionaryDefinitionTwo: ChineseDefinition =
    DefinitionEntry.buildChineseDefinition(
      dummyWiktionaryDefinitionEntryTwo,
      PartOfSpeech.NOUN
    )

  val niHao: Word =
    Word.fromToken("你好", Language.CHINESE).copy(tag = PartOfSpeech.NOUN)

  describe("When getting definitions for a single word") {

    def stubFor[T](entries: List[T]): Unit = {
      when(
        elasticsearchClientMock
          .findFromCacheOrRefetch[T](
            any(classOf[ElasticsearchSearchRequest[T]])
          )(
            any(classOf[ClassTag[T]]),
            any(classOf[Reads[T]]),
            any(classOf[Writes[T]])
          )
      ).thenReturn(
        Future.successful(
          entries
        )
      )
    }

    it("Does not enhance non-chinese definitions") {
      // This will delegate to the base LanguageDefinitionService implementation
      // So the assertions may fail if that changes.
      stubFor[CEDICTDefinitionEntry](List(dummyCedictDefinitionEntry))
      stubFor[WiktionaryDefinitionEntry](List(dummyWiktionaryDefinitionEntry))

      chineseDefinitionService
        .getDefinitions(Language.CHINESE, niHao)
        .map { definitions =>
          assert(definitions.size == 2)
          assert(definitions.exists(_.eq(dummyCedictDefinition)))
          assert(definitions.exists(_.eq(dummyWiktionaryDefinition)))
        }
    }

    describe("can enhance chinese definitions") {
      it("and returns cedict definitions if no wiktionary are found") {
        stubFor[CEDICTDefinitionEntry](List(dummyCedictDefinitionEntry))
        stubFor[WiktionaryDefinitionEntry](List())

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, niHao)
          .map { definitions =>
            assert(definitions.size == 1)
            assert(definitions.exists(_.eq(dummyCedictDefinition)))
          }
      }

      it(
        "and returns wiktionary definitions if no cedict definitions are found"
      ) {
        stubFor[CEDICTDefinitionEntry](List())
        stubFor[WiktionaryDefinitionEntry](List(dummyWiktionaryDefinitionEntry))

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, niHao)
          .map { definitions =>
            assert(definitions.size == 1)
            assert(definitions.exists(_.eq(dummyWiktionaryDefinition)))
          }
      }

      it("combines cedict and wiktionary definitions correctly") {
        stubFor[CEDICTDefinitionEntry](List(dummyCedictDefinitionEntry))
        stubFor[WiktionaryDefinitionEntry](
          List(
            dummyWiktionaryDefinitionEntry,
            dummyWiktionaryDefinitionEntryTwo
          )
        )

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, niHao)
          .map { definitions =>
            assert(definitions.size == 1)
            val combined = definitions.head
            assert(
              combined.subdefinitions == dummyCedictDefinitionEntry.subdefinitions
            )
            assert(combined.tag == dummyWiktionaryDefinitionEntry.tag.get)
            assert(
              combined.examples.contains(
                (dummyWiktionaryDefinitionEntry.examples ++ dummyWiktionaryDefinitionEntryTwo.examples).flatten
              )
            )
            combined match {
              case c: ChineseDefinition =>
                assert(c.pronunciation.pinyin == "ni hao")
                assert(
                  c.simplified.get == dummyCedictDefinitionEntry.simplified
                )
                assert(
                  c.traditional.get.head == dummyCedictDefinitionEntry.traditional
                )
              case other =>
                fail(s"We aren't returning Chinese definitions, got $other")
            }
            assert(combined.definitionLanguage == Language.ENGLISH)
            assert(combined.source == DefinitionSource.MULTIPLE)
            assert(combined.token == dummyCedictDefinitionEntry.token)
          }
      }

      it(
        "combines cedict and wiktionary definitions correctly when cedict entries are missing key data"
      ) {
        stubFor[CEDICTDefinitionEntry](
          List(dummyCedictDefinitionEntry.copy(subdefinitions = List()))
        )
        stubFor[WiktionaryDefinitionEntry](
          List(
            dummyWiktionaryDefinitionEntry,
            dummyWiktionaryDefinitionEntryTwo
          )
        )

        chineseDefinitionService
          .getDefinitions(Language.ENGLISH, niHao)
          .map { definitions =>
            assert(definitions.size == 2)
            val combinedOne = definitions.head
            val combinedTwo = definitions(1)

            // Cedict sourced data should be the same for all
            List(combinedOne, combinedTwo) map {
              case c: ChineseDefinition =>
                assert(c.pronunciation.pinyin == "ni hao")
                assert(
                  c.simplified.get == dummyCedictDefinitionEntry.simplified
                )
                assert(
                  c.traditional.get.head == dummyCedictDefinitionEntry.traditional
                )
              case other =>
                fail(s"We aren't returning Chinese definitions, got $other")
            }

            assert(
              definitions.forall(_.token == dummyCedictDefinitionEntry.token)
            )

            // Generated data should be the same for all
            assert(definitions.forall(_.definitionLanguage == Language.ENGLISH))
            assert(definitions.forall(_.source == DefinitionSource.MULTIPLE))

            assert(
              combinedOne.subdefinitions == dummyWiktionaryDefinitionEntry.subdefinitions
            )
            assert(combinedOne.tag == dummyWiktionaryDefinitionEntry.tag.get)
            assert(
              combinedOne.examples == dummyWiktionaryDefinitionEntry.examples
            )
            assert(
              combinedTwo.subdefinitions == dummyWiktionaryDefinitionEntryTwo.subdefinitions
            )
            assert(combinedTwo.tag == dummyWiktionaryDefinitionEntryTwo.tag.get)
            assert(
              combinedTwo.examples == dummyWiktionaryDefinitionEntryTwo.examples
            )
          }
      }
    }
  }
}
