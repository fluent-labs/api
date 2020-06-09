package com.foreignlanguagereader.api.domain

import play.api.libs.json._

object Language extends Enumeration {
  type Language = Value
  val CHINESE, ENGLISH, SPANISH = Value

  implicit val languageFormat: Format[Language] = new Format[Language] {
    def reads(json: JsValue) = JsSuccess(Language.withName(json.as[String]))
    def writes(language: Language.Language) = JsString(language.toString)
  }
}
