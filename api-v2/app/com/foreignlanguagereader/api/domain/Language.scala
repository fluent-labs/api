package com.foreignlanguagereader.api.domain

import com.foreignlanguagereader.api.domain
import play.api.libs.json._
import play.api.mvc.PathBindable
import sangria.macros.derive.{EnumTypeDescription, EnumTypeName, deriveEnumType}
import sangria.schema.EnumType

object Language extends Enumeration {
  type Language = Value
  val CHINESE: Value = Value("CHINESE")
  val CHINESE_TRADITIONAL: domain.Language.Value = Value("CHINESE_TRADITIONAL")
  val ENGLISH: Value = Value("ENGLISH")
  val SPANISH: Value = Value("SPANISH")

  def fromString(s: String): Option[Language] =
    Language.values.find(_.toString == s)

  implicit val languageFormat: Format[Language] = new Format[Language] {
    def reads(json: JsValue): JsResult[Language] =
      fromString(json.toString) match {
        case Some(language) => JsSuccess(language)
        case None           => JsError("Invalid language")
      }
    def writes(language: Language.Language): JsString =
      JsString(language.toString)
  }

  implicit def pathBinder: PathBindable[Language] =
    new PathBindable[Language] {
      override def bind(key: String, value: String): Either[String, Language] =
        fromString(value) match {
          case Some(language) =>
            Right(language)
          case _ => Left(value)
        }
      override def unbind(key: String, value: Language): String = value.toString
    }

  val graphqlType: EnumType[Language] = deriveEnumType[Language](
    EnumTypeName("Language"),
    EnumTypeDescription("A human language")
  )
}
