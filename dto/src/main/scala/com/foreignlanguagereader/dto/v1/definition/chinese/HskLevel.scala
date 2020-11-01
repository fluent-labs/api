package com.foreignlanguagereader.dto.v1.definition.chinese

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsValue}
import sangria.macros.derive.deriveEnumType
import sangria.schema.EnumType

object HskLevel extends Enumeration {
  type HSKLevel = Value

  val ONE: Value = Value("1")
  val TWO: Value = Value("2")
  val THREE: Value = Value("3")
  val FOUR: Value = Value("4")
  val FIVE: Value = Value("5")
  val SIX: Value = Value("6")
  val NONE: Value = Value("")

  implicit val hskLevelFormat: Format[HSKLevel] = new Format[HSKLevel] {
    def reads(json: JsValue): JsResult[Value] =
      JsError("We don't read these in, only export")
    def writes(level: HskLevel.HSKLevel): JsString = JsString(level.toString)
  }
  implicit val graphQlType: EnumType[HskLevel.Value] =
    deriveEnumType[HskLevel.Value]()
}
