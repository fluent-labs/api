package com.foreignlanguagereader.api.controller.v1

import com.foreignlanguagereader.api.error.BadInputException
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.RequestPath
import com.foreignlanguagereader.domain.service.UserService
import com.foreignlanguagereader.domain.user.User
import com.foreignlanguagereader.dto.v1.user.Login
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import play.libs.{Json => JavaJson}

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject() (
    val controllerComponents: ControllerComponents,
    userService: UserService,
    metrics: MetricsReporter,
    implicit val ec: ExecutionContext
) extends BaseController {
  val logger: Logger = Logger(this.getClass)

  implicit val loginReader: Reads[Login] =
    ((JsPath \ "username").read[String] and (JsPath \ "password").read[String])(
      (username, password) => new Login(username, password)
    )

  def login: Action[JsValue] =
    Action.async(parse.json) { request =>
      {
        request.body.validate[Login] match {
          case JsSuccess(login: Login, _) => handleLogin(login)
          case JsError(errors)            => handleInvalidLogin(errors)
        }
      }
    }

  def handleLogin(login: Login): Future[Result] = {
    userService.login(login.getUsername, login.getPassword) match {
      case Some(_) => Future.successful(Ok("Logged in"))
      case None    => Future.successful(Unauthorized("Login unsuccessful"))
    }
  }

  def handleInvalidLogin(
      errors: Seq[(JsPath, Seq[JsonValidationError])]
  ): Future[Result] = {
    logger.error(
      s"Invalid login request: $errors"
    )
    metrics.reportFailedLogin()
    metrics.reportBadRequest(RequestPath.LOGIN)
    Future {
      BadRequest(
        JavaJson.stringify(
          JavaJson.toJson(
            new BadInputException(
              "Invalid login request, please try again"
            )
          )
        )
      )
    }
  }

  def register: Action[JsValue] =
    Action.async(parse.json) { request =>
      {
        request.body.validate[Login] match {
          case JsSuccess(login: Login, _) => handleRegistration(login)
          case JsError(errors)            => handleInvalidRegistration(errors)
        }
      }
    }

  def handleRegistration(
      login: Login
  ): Future[Result] = {
    userService.getUser(login.getUsername) match {
      case None =>
        userService.register(User(login.getUsername, login.getPassword, ""))
        Future.successful(Ok("Registered"))
      case Some(_) =>
        logger.error(
          s"User ${login.getUsername} cannot be registered because they already have an account"
        )
        Future.successful(Conflict(s"User ${login.getUsername} already exists"))
    }
  }

  def handleInvalidRegistration(
      errors: Seq[(JsPath, Seq[JsonValidationError])]
  ): Future[Result] = {
    logger.error(
      s"Invalid registration request: $errors"
    )
    metrics.reportBadRequest(RequestPath.REGISTER)
    Future {
      BadRequest(
        JavaJson.stringify(
          JavaJson.toJson(
            new BadInputException(
              "Invalid registration request, please try again"
            )
          )
        )
      )
    }
  }
}
