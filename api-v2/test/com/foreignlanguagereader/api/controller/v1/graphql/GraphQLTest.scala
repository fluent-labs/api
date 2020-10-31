package com.foreignlanguagereader.api.controller.v1.graphql

import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.word.Word
import com.foreignlanguagereader.api.service.definition.{
  ChineseDefinitionService,
  DefinitionService,
  EnglishDefinitionService,
  SpanishDefinitionService
}
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.Definition
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

// Shim because scalatestplus is behind scalatest, so dependencies collide
// More: https://github.com/scalatest/scalatest/issues/1734
abstract class PlaySpec
    extends AnyWordSpec
    with Matchers
    with OptionValues
    with WsScalaTestClient

class MockDefinitionService
    extends DefinitionService(
      Mockito.mock(classOf[ChineseDefinitionService]),
      Mockito.mock(classOf[EnglishDefinitionService]),
      Mockito.mock(classOf[SpanishDefinitionService]),
      Mockito.mock(classOf[ExecutionContext])
    )
    with MockitoSugar {
  val mock: DefinitionService = {
    val m = Mockito.mock(classOf[DefinitionService])
    when(
      m.getDefinition(
        Language.ENGLISH,
        Language.ENGLISH,
        Word.fromToken("test", Language.ENGLISH)
      )
    ).thenReturn(Future.successful(List()))
    when(
      m.getDefinition(
        Language.CHINESE,
        Language.ENGLISH,
        Word.fromToken("你好", Language.CHINESE)
      )
    ).thenReturn(Future.successful(List()))
    m
  }
  override def getDefinition(
      wordLanguage: Language,
      definitionLanguage: Language,
      word: Word
  ): Future[List[Definition]] =
    mock.getDefinition(wordLanguage, definitionLanguage, word)
}

class GraphQLTest
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with MockitoSugar {

  val application: Application = new GuiceApplicationBuilder()
    .overrides(bind[DefinitionService].to[MockDefinitionService])
    .build()

  "Get definitions" must {
    "get English definitions" in {
      val englishDefinitionsQuery: String =
        "{" +
          "\"query\": \"{ " +
          "definition(wordLanguage: ENGLISH, definitionLanguage: ENGLISH, token:\\\"test\\\") { " +
          "...on GenericDefinitionDTO { " +
          "subdefinitions " +
          "tag " +
          "examples " +
          "} " +
          "} " +
          "}\", " +
          "\"variables\": {} " +
          "}"

      val request = FakeRequest("POST", "/graphql")
        .withHeaders(("Content-Type", "application/json"))
        .withBody(englishDefinitionsQuery)

      val response = route(application, request).get
      status(response) mustBe OK
      val result = contentAsString(response)

      result must include("data")
      result mustNot include("errors")
    }

    "get Chinese definitions" in {
      val chineseDefinitionsQuery: String =
        "{" +
          "\"query\": \"{ " +
          "definition(wordLanguage: CHINESE, definitionLanguage: ENGLISH, token:\\\"你好\\\") { " +
          "...on ChineseDefinitionDTO { " +
          "subdefinitions " +
          "tag " +
          "examples " +
          "pronunciation { " +
          "pinyin " +
          "ipa " +
          "zhuyin " +
          "wadeGiles " +
          "tones " +
          "} " +
          "traditional " +
          "simplified " +
          "hsk " +
          "} " +
          "} " +
          "}\", " +
          "\"variables\": {} " +
          "}"

      val request = FakeRequest("POST", "/graphql")
        .withHeaders(("Content-Type", "application/json"))
        .withBody(chineseDefinitionsQuery)

      val response = route(application, request).get
      status(response) mustBe OK
      val result = contentAsString(response)

      result must include("data")
      result mustNot include("errors")
    }
  }
}
