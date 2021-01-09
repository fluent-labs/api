package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Count.Count
import com.foreignlanguagereader.content.types.internal.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.WordTense.WordTense
import com.foreignlanguagereader.content.types.internal.word.{
  Count,
  GrammaticalGender,
  PartOfSpeech,
  WordTense
}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcType

import scala.reflect.ClassTag

object EnumImplicits {
  implicit val countMapper: JdbcType[Count] =
    buildMapper[Count]("count", s => Count.fromString(s))
  implicit val genderMapper: JdbcType[GrammaticalGender] =
    buildMapper[GrammaticalGender](
      "gender",
      s => GrammaticalGender.fromString(s)
    )
  implicit val languageMapper: JdbcType[Language] =
    buildMapper[Language]("language", s => Language.fromString(s))
  implicit val partOfSpeechMapper: JdbcType[PartOfSpeech] =
    buildMapper[PartOfSpeech]("partOfSpeech", s => PartOfSpeech.fromString(s))
  implicit val wordTenseMapper: JdbcType[WordTense] =
    buildMapper[WordTense]("tense", s => WordTense.fromString(s))

  def buildMapper[T](
      name: String,
      serializer: String => Option[T]
  )(implicit tag: ClassTag[T]): JdbcType[T] = {
    MappedColumnType.base[T, String](
      e => e.toString,
      s =>
        serializer.apply(s) match {
          case Some(value) => value
          case None =>
            throw new IllegalArgumentException(
              s"Invalid value $s for enum $name"
            )
        }
    )
  }
}
