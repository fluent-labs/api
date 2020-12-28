package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Count.Count
import com.foreignlanguagereader.content.types.internal.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.WordTense.WordTense
import com.foreignlanguagereader.domain.repository.EnumImplicits._
import slick.jdbc.H2Profile.api._

/**
  * language: Language,
  * token: String,
  * tag: PartOfSpeech,
  * lemma: String,
  * definitions: Seq[Definition],
  * gender: Option[GrammaticalGender],
  * number: Option[Count],
  * proper: Option[Boolean],
  * tense: Option[WordTense],
  * processedToken: String
  *
  * @param tag
  */

class WordDAO(tag: Tag)
    extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
  def language: Rep[Language] = column[Language]("language")
  def token: Rep[String] = column[String]("token")
  def tag: Rep[PartOfSpeech] = column[PartOfSpeech]("tag")
  def lemma: Rep[String] = column[String]("lemma")
  // definitions
  def gender = column[Option[GrammaticalGender]]("gender")
  def number = column[Option[Count]]("count")
  def proper = column[Boolean]("proper")
  def tense = column[Option[WordTense]]("tense")
  def processedToken: Rep[String] = column[String]("processedtoken")

  def name = column[String]("COF_NAME", O.PrimaryKey)
  def supID = column[Int]("SUP_ID")
  def price = column[Double]("PRICE")
  def sales = column[Int]("SALES", O.Default(0))
  def total = column[Int]("TOTAL", O.Default(0))
  def * = (name, supID, price, sales, total)
}

class WordRepository {}
