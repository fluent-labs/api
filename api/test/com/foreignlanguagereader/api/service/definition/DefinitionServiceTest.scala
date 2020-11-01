package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.{
  ChineseDefinition,
  DefinitionSource,
  GenericDefinition
}
import com.foreignlanguagereader.domain.internal.word.{PartOfSpeech, Word}
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class DefinitionServiceTest extends AsyncFunSpec with MockitoSugar {
  val mockChineseService: ChineseDefinitionService =
    mock[ChineseDefinitionService]
  val mockEnglishService: EnglishDefinitionService =
    mock[EnglishDefinitionService]
  val mockSpanishService: SpanishDefinitionService =
    mock[SpanishDefinitionService]
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val definitionService = new DefinitionService(
    mockChineseService,
    mockEnglishService,
    mockSpanishService,
    ec
  )

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
    ipa = "",
    tag = PartOfSpeech.NOUN,
    examples = Some(List("example 1", "example 2")),
    definitionLanguage = Language.ENGLISH,
    wordLanguage = Language.ENGLISH,
    source = DefinitionSource.MULTIPLE,
    token = "anything"
  )

  val suoYouDe: Word = Word.fromToken("所有的", Language.CHINESE)
  val anything: Word = Word.fromToken("anything", Language.ENGLISH)
  val cualquier: Word = Word.fromToken("cualquier", Language.SPANISH)

  describe("When getting definitions for a single word") {
    it("can get definitions in Chinese") {
      when(mockChineseService.getDefinitions(Language.ENGLISH, suoYouDe))
        .thenReturn(Future.successful(List(dummyChineseDefinition)))

      definitionService
        .getDefinition(Language.CHINESE, Language.ENGLISH, suoYouDe)
        .map { response =>
          assert(response.size == 1)
          assert(response(0) == dummyChineseDefinition)
        }
    }

    it("can get definitions in English") {
      when(mockEnglishService.getDefinitions(Language.CHINESE, anything))
        .thenReturn(Future.successful(List(dummyGenericDefinition)))

      definitionService
        .getDefinition(Language.ENGLISH, Language.CHINESE, anything)
        .map { response =>
          assert(response.size == 1)
          assert(response(0) == dummyGenericDefinition)
        }
    }

    it("can get definitions in Spanish") {
      when(mockSpanishService.getDefinitions(Language.ENGLISH, cualquier))
        .thenReturn(Future.successful(List(dummyGenericDefinition)))

      definitionService
        .getDefinition(Language.SPANISH, Language.ENGLISH, cualquier)
        .map { response =>
          assert(response.size == 1)
          assert(response(0) == dummyGenericDefinition)
        }
    }
  }
}
