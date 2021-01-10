package com.foreignlanguagereader.domain.dao

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

class VocabularySchema(tag: Tag)
    extends Table[(String, Int)](tag, "Vocabulary") {
  val users = TableQuery[UserSchema]
  val words = TableQuery[WordSchema]

  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def email: Rep[String] = column[String]("email")
  def wordId: Rep[Int] = column[Int]("word")

  val user = foreignKey("vocabulary_email_fk", email, users)(_.email)
  val word = foreignKey("vocabulary_word_fk", wordId, words)(_.id)

  def * : ProvenShape[(String, Int)] = (email, wordId)
}
