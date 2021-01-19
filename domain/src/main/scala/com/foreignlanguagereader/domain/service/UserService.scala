package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.domain.user.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject() (implicit
    val ec: ExecutionContext
) extends IdentityService[User] {
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

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    Future.successful(None)
}
