package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.domain.user.User
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserService @Inject() (implicit
    val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  def register(user: User): User = {
    logger.info(s"Registering user ${user.email}")
    user.copy(password = "")
  }

  def login(user: User): Option[User] = {
    logger.info(s"Logging in user ${user.email}")
    Some(user.copy(password = ""))
  }

  def getUser(email: String): Option[User] = {
    logger.info(s"Getting info for user $email")
    None
  }
}
