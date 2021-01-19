package com.foreignlanguagereader.domain.dao

import com.foreignlanguagereader.domain.client.database.DatabaseConnection.dc.profile.api._
import com.foreignlanguagereader.domain.user.User
import slick.lifted.ProvenShape

class UserSchema(tag: Tag) extends Table[User](tag, "User") {
  def email: Rep[String] = column[String]("email", O.Unique)
  def name: Rep[String] = column[String]("name")
  def password: Rep[String] = column[String]("password")

  def * : ProvenShape[User] = (email, name, password).mapTo[User]
}
