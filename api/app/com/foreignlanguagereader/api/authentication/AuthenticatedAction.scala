package com.foreignlanguagereader.api.authentication

import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.service.AuthenticationService

import javax.inject.Inject
import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticatedAction @Inject() (
    bodyParser: BodyParsers.Default,
    authService: AuthenticationService,
    metrics: MetricsReporter
)(implicit ec: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  private val headerTokenRegex = """Bearer (.+?)""".r

  override def invokeBlock[A](
      request: Request[A],
      block: AuthenticatedRequest[A] => Future[Result]
  ): Future[Result] =
    extractBearerToken(request) map { token =>
      authService.validateJwt(token) match {
        case Success(claim) =>
          block(AuthenticatedRequest(claim, token, request))
        case Failure(t) => Future.successful(Results.Unauthorized(t.getMessage))
      }
    } getOrElse Future.successful(Results.Unauthorized)

  private[this] def extractBearerToken[A](request: Request[A]): Option[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => token
    }
}
