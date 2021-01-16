package com.foreignlanguagereader.domain.service

import com.foreignlanguagereader.domain.user.User

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserService @Inject() (implicit
    val ec: ExecutionContext
) {
  def register(user: User): User = {
    user.copy(password = "")
  }

  def login(user: User): Option[User] = {
    Some(user.copy(password = ""))
  }
}
