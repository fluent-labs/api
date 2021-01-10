package com.foreignlanguagereader.domain.dao

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

case class UserDAO(
    email: String,
    name: String,
    password: String
)

class UserSchema(tag: Tag) extends Table[UserDAO](tag, "User") {
  def email: Rep[String] = column[String]("email", O.PrimaryKey)
  def name: Rep[String] = column[String]("name")
  def password: Rep[String] = column[String]("password")

  def * : ProvenShape[UserDAO] = (email, name, password).mapTo[UserDAO]
}
