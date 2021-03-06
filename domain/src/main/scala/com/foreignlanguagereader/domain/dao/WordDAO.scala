package com.foreignlanguagereader.domain.dao

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.domain.client.database.DatabaseConnection.dc.profile.api._
import com.foreignlanguagereader.domain.repository.EnumImplicits._
import slick.lifted.ProvenShape

case class WordDAO(
    language: Language,
    token: String,
    tag: PartOfSpeech,
    lemma: String
)

class WordSchema(tag: Tag) extends Table[WordDAO](tag, "Word") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def language: Rep[Language] = column[Language]("language")
  def token: Rep[String] = column[String]("token")
  def wordTag: Rep[PartOfSpeech] = column[PartOfSpeech]("tag")
  def lemma: Rep[String] = column[String]("lemma")

  def * : ProvenShape[WordDAO] =
    (language, token, wordTag, lemma).mapTo[WordDAO]
}
