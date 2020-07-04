package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition,
  GenericDefinition
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.domain.word.PartOfSpeech
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
    Some(PartOfSpeech.NOUN),
    List("example 1", "example 2"),
    "ni3 hao3",
    "你好",
    "你好",
    Language.ENGLISH,
    DefinitionSource.MULTIPLE,
    token = "你好"
  )
  val dummyGenericDefinition = GenericDefinition(
    List("definition 1", "definition 2"),
    Some(PartOfSpeech.NOUN),
    List("example 1", "example 2"),
    Language.ENGLISH,
    Language.ENGLISH,
    DefinitionSource.MULTIPLE,
    "anything"
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

    it("can get definitions in English") {
      when(mockEnglishService.getDefinitions(Language.CHINESE, "anything"))
        .thenReturn(Future.successful(Some(List(dummyGenericDefinition))))

      definitionService
        .getDefinition(Language.ENGLISH, Language.CHINESE, "anything")
        .map { result =>
          assert(result.isDefined)
          val response = result.get
          assert(response.size == 1)
          assert(response(0) == dummyGenericDefinition)
        }
    }

    it("can get definitions in Spanish") {
      when(mockSpanishService.getDefinitions(Language.ENGLISH, "cualquier"))
        .thenReturn(Future.successful(Some(List(dummyGenericDefinition))))

      definitionService
        .getDefinition(Language.SPANISH, Language.ENGLISH, "cualquier")
        .map { result =>
          assert(result.isDefined)
          val response = result.get
          assert(response.size == 1)
          assert(response(0) == dummyGenericDefinition)
        }
    }
  }

  describe("When getting definitions for multiple words") {
    it("can correctly combine both results") {
      val secondDummyDefinition = dummyGenericDefinition.copy(token = "another")
      when(mockEnglishService.getDefinitions(Language.CHINESE, "anything"))
        .thenReturn(Future.successful(Some(List(dummyGenericDefinition))))
      when(mockEnglishService.getDefinitions(Language.CHINESE, "another"))
        .thenReturn(
          Future.successful(
            Some(List(dummyGenericDefinition.copy(token = "another")))
          )
        )

      definitionService
        .getDefinitions(
          Language.ENGLISH,
          Language.CHINESE,
          List("anything", "another")
        )
        .map { result: Map[String, Option[Seq[Definition]]] =>
          assert(result.get("anything").isDefined)
          assert(result.get("another").isDefined)

          val anything = result("anything").get
          assert(anything.size == 1)
          assert(anything(0) == dummyGenericDefinition)

          val another = result("another").get
          assert(another.size == 1)
          assert(another(0) == secondDummyDefinition)
        }
    }
    it("appropriately handles missing results") {
      when(mockEnglishService.getDefinitions(Language.CHINESE, "anything"))
        .thenReturn(Future.successful(Some(List(dummyGenericDefinition))))
      when(mockEnglishService.getDefinitions(Language.CHINESE, "another"))
        .thenReturn(Future.successful(None))

      definitionService
        .getDefinitions(
          Language.ENGLISH,
          Language.CHINESE,
          List("anything", "another")
        )
        .map { result =>
          assert(result.get("anything").isDefined)
          assert(result.get("another").isDefined)

          val anything = result("anything").get
          assert(anything.size == 1)
          assert(anything(0) == dummyGenericDefinition)

          assert(result("another").isEmpty)
        }
    }
  }
}
