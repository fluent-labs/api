package com.foreignlanguagereader.api

import com.foreignlanguagereader.api
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue}

object Language extends Enumeration {
  type Language = Value
  val CHINESE, ENGLISH, SPANISH = Value

  implicit val languageFormat: Format[Language] = new Format[Language] {
    def reads(json: JsValue) = JsSuccess(Language.withName(json.as[String]))
    def writes(language: Language.Language) = JsString(language.toString)
  }
}

object ReadinessStatus extends Enumeration {
  type ReadinessStatus = Value
  val UP, DOWN, DEGRADED = Value

  implicit val readinessStatusFormat: Format[ReadinessStatus] =
    new Format[ReadinessStatus] {
      def reads(json: JsValue) =
        JsSuccess(ReadinessStatus.withName(json.as[String]))
      def writes(status: ReadinessStatus.ReadinessStatus) =
        JsString(status.toString)
    }
}

object DefinitionSource extends Enumeration {
  type DefinitionSource = Value
  val WIKTIONARY, CEDICT, MULTIPLE = Value
}

object HSKLevel extends Enumeration {
  type HSKLevel = Value

  val ONE: api.HSKLevel.Value = Value("1")
  val TWO: api.HSKLevel.Value = Value("2")
  val THREE: api.HSKLevel.Value = Value("3")
  val FOUR: api.HSKLevel.Value = Value("4")
  val FIVE: api.HSKLevel.Value = Value("5")
  val SIX: api.HSKLevel.Value = Value("6")
  val NONE: api.HSKLevel.Value = Value("")

  implicit val hskLevelFormat: Format[HSKLevel] = new Format[HSKLevel] {
    def reads(json: JsValue): JsResult[api.HSKLevel.Value] =
      JsSuccess(HSKLevel.withName(json.as[String]))
    def writes(level: HSKLevel.HSKLevel) = JsString(level.toString)
  }
}
