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

object EnumImplicits {
  implicit val countMapper: JdbcType[Count] =
    buildMapper[Count](s => Count.fromString(s))
  implicit val genderMapper: JdbcType[GrammaticalGender] =
    buildMapper[GrammaticalGender](s => GrammaticalGender.fromString(s))
  implicit val languageMapper: JdbcType[Language] =
    buildMapper[Language](s => Language.fromString(s))
  implicit val partOfSpeechMapper: JdbcType[PartOfSpeech] =
    buildMapper[PartOfSpeech](s => PartOfSpeech.fromString(s))
  implicit val wordTenseMapper: JdbcType[WordTense] =
    buildMapper[WordTense](s => WordTense.fromString(s))

  def buildMapper[T](
      serializer: String => Option[T]
  ): JdbcType[T] = {
    MappedColumnType.base[T, String](
      e => e.toString,
      s => serializer.apply(s).getOrElse("ERROR")
    )
  }
}
