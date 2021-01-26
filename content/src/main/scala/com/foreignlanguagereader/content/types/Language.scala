package com.foreignlanguagereader.content.types

import cats.implicits._
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.PathBindable

object Language extends Enumeration {
  type Language = Value
  val CHINESE: Value = Value("CHINESE")
  val CHINESE_TRADITIONAL: Language.Value = Value("CHINESE_TRADITIONAL")
  val ENGLISH: Value = Value("ENGLISH")
  val SPANISH: Value = Value("SPANISH")
  val UNKNOWN: Value = Value("UNKNOWN")

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
}
