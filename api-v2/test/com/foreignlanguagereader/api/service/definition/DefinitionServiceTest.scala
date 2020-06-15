package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.combined.ChineseDefinition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
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

  describe("When getting definitions for a single word") {
    it("can get definitions in Chinese") {
      when(mockChineseService.getDefinitions(Language.ENGLISH, "所有的"))
        .thenReturn(Future.successful(Some(List(dummyChineseDefinition))))

      definitionService
        .getDefinition(Language.CHINESE, Language.ENGLISH, "所有的")
        .map { result =>
          assert(result.isDefined)
          val response = result.get
          assert(response.size == 1)
          assert(response(0) == dummyChineseDefinition)
        }
    }
  }
}
