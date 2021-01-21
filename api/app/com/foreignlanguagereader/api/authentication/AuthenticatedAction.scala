package com.foreignlanguagereader.api.authentication

import com.foreignlanguagereader.api.error.ServiceException
import com.foreignlanguagereader.api.metrics.ApiMetricReporter
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.RequestPath.RequestPath
import com.foreignlanguagereader.domain.service.AuthenticationService
import play.api.Logger

import javax.inject.Inject
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.Results.Unauthorized
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticatedAction @Inject() (
    bodyParser: BodyParsers.Default,
    authService: AuthenticationService,
    metrics: MetricsReporter
)(implicit ec: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] {
  val logger: Logger = Logger(this.getClass)

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  private val headerTokenRegex = """Bearer (.+?)""".r

  val unauthorizedMessage: String =
    Json.stringify(
      Json.toJson(ServiceException("You are not authorized for this endpoint"))
    )
  val unauthorizedResponse: Future[Result] =
    Future.successful(Unauthorized(unauthorizedMessage))

  override def invokeBlock[A](
      request: Request[A],
      block: AuthenticatedRequest[A] => Future[Result]
  ): Future[Result] = {
    val path = ApiMetricReporter.getLabelFromPath(request.path)

    extractBearerToken(request, path) map { token =>
      authService.validateJwt(token) match {
        case Success(claim) =>
          block(AuthenticatedRequest(claim, token, request))
        case Failure(t) =>
          logger.error(
            s"Invalid token provided on path $path: ${t.getMessage}, token: $token",
            t
          )
          metrics.reportBadRequestToken(path)
          unauthorizedResponse
      }
    } getOrElse {
      metrics.reportUnauthenticatedRequest(path)
      logger.error(
        s"Unauthenticated request to endpoint requiring authentication: ${request.path}"
      )
      unauthorizedResponse
    }
  }

  private[this] def extractBearerToken[A](
      request: Request[A],
      path: RequestPath
  ): Option[String] = {
    logger.info("Extracting bearer token")
    request.headers.get(HeaderNames.AUTHORIZATION) match {
      case Some(headerTokenRegex(token)) =>
        logger.info("Successfully got token")
        Some(token)
      case Some(badToken) =>
        metrics.reportBadRequestToken(path)
        logger.error(s"Malformed token found on path $path: $badToken")
        None
      case None =>
        metrics.reportUnauthenticatedRequest(path)
        logger.error(s"No token provided on path $path")
        None
    }
  }
}
