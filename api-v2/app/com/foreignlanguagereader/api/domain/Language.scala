package com.foreignlanguagereader.api.domain

import play.api.libs.json._
import play.api.mvc.PathBindable

object Language extends Enumeration {
  type Language = Value
  val CHINESE: Value = Value("CHINESE")
  val ENGLISH: Value = Value("ENGLISH")
  val SPANISH: Value = Value("SPANISH")

  def isLanguage(s: String): Boolean = values.exists(_.toString == s)

  implicit val languageFormat: Format[Language] = new Format[Language] {
    def reads(json: JsValue) = JsSuccess(Language.withName(json.as[String]))
    def writes(language: Language.Language) = JsString(language.toString)
  }

  implicit def pathBinder: PathBindable[Language] =
    new PathBindable[Language] {
      override def bind(key: String, value: String): Either[String, Language] =
        value match {
          case language if Language.isLanguage(language) =>
            Right(Language.withName(value))
          case _ => Left(value)
        }
      override def unbind(key: String, value: Language): String = value.toString
    }
}
