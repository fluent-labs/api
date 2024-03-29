package io.fluentlabs.domain.dao

import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.internal.word.PartOfSpeech.PartOfSpeech
import io.fluentlabs.domain.client.database.DatabaseConnection.dc.profile.api._
import io.fluentlabs.domain.repository.EnumImplicits._
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
