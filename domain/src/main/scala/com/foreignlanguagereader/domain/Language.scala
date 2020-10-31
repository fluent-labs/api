package com.foreignlanguagereader.domain

import cats.implicits._
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.PathBindable
import sangria.macros.derive.{EnumTypeDescription, EnumTypeName, deriveEnumType}
import sangria.schema.EnumType

object Language extends Enumeration {
  type Language = Value
  val CHINESE: Value = Value("CHINESE")
  val CHINESE_TRADITIONAL: Language.Value = Value("CHINESE_TRADITIONAL")
  val ENGLISH: Value = Value("ENGLISH")
  val SPANISH: Value = Value("SPANISH")

  def fromString(s: String): Option[Language] =
    Language.values.find(_.toString === s)

  implicit val reads: Reads[Language] = Reads.enumNameReads(Language)
  implicit val writes: Writes[Language] = Writes.enumNameWrites

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
