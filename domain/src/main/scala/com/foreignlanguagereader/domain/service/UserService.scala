package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.domain.user.User
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject() (implicit
    val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  def register(user: User): User = {
    logger.info(s"Registering user ${user.email}")
    user.copy(password = "")
  }

  def login(username: String, password: String): Option[User] = {
    logger.info(s"Logging in user $username")
    Some(User(username, "", ""))
  }

  def getUser(email: String): Option[User] = {
    logger.info(s"Getting info for user $email")
    None
  }
}
