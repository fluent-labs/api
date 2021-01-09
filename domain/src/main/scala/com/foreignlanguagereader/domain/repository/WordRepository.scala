package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Count.Count
import com.foreignlanguagereader.content.types.internal.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.WordTense.WordTense
import com.foreignlanguagereader.domain.repository.EnumImplicits._
import slick.jdbc.H2Profile.api._

class WordDAO(tag: Tag)
    extends Table[
      (
          Language,
          String,
          PartOfSpeech,
          String,
          Option[GrammaticalGender],
          Option[Count],
          Boolean,
          Option[WordTense],
          String
      )
    ](tag, "Word") {
  def language: Rep[Language] = column[Language]("language")
  def token: Rep[String] = column[String]("token")
  def wordTag: Rep[PartOfSpeech] = column[PartOfSpeech]("tag")
  def lemma: Rep[String] = column[String]("lemma")
  // definitions
  def gender: Rep[Option[GrammaticalGender]] =
    column[Option[GrammaticalGender]]("gender")
  def number: Rep[Option[Count]] = column[Option[Count]]("count")
  def proper: Rep[Boolean] = column[Boolean]("proper")
  def tense: Rep[Option[WordTense]] = column[Option[WordTense]]("tense")
  def processedToken: Rep[String] = column[String]("processedtoken")

  def * =
    (
      language,
      token,
      wordTag,
      lemma,
      gender,
      number,
      proper,
      tense,
      processedToken
    )
}

class WordRepository {}
