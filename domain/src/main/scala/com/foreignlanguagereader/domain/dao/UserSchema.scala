package com.foreignlanguagereader.domain.dao

import com.foreignlanguagereader.domain.client.database.DatabaseConnection.dc.profile.api._
import com.foreignlanguagereader.domain.user.User
import com.mohiva.play.silhouette.api.Identity
import slick.lifted.ProvenShape

import java.util.UUID

class UserSchema(tag: Tag) extends Table[User](tag, "User") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def email: Rep[String] = column[String]("email", O.Unique)
  def name: Rep[String] = column[String]("name")
  def password: Rep[String] = column[String]("password")

  def * : ProvenShape[User] = (id, email, name, password).mapTo[User]
}
