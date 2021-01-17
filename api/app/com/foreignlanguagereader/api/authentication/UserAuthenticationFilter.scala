package com.foreignlanguagereader.api.authentication

import akka.stream.Materializer
import com.foreignlanguagereader.api.error.ServiceException
import com.foreignlanguagereader.domain.user.DefaultEnv
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{Filter, PlayBodyParsers, RequestHeader, Result}

import javax.inject.Inject
import scala.concurrent.Future

class UserAuthenticationFilter @Inject() (implicit
    val mat: Materializer,
    silhouette: Silhouette[DefaultEnv],
    bodyParsers: PlayBodyParsers
) extends Filter {

  val unauthenticatedRoutes = Set(
    "/health",
    "/metrics",
    "/readiness",
    "/v1/user/login",
    "/v1/user/register"
  )

  def canAccessRoute(path: String, identity: Option[DefaultEnv#I]): Boolean =
    unauthenticatedRoutes.contains(path) || identity.nonEmpty

  val unauthorizedMessage: String =
    Json.stringify(
      Json.toJson(
        ServiceException("You must be logged in to access this endpoint.")
      )
    )

  override def apply(
      next: RequestHeader => Future[Result]
  )(request: RequestHeader): Future[Result] = {

    // As the body of request can't be parsed twice in Play we should force
    // to parse empty body for UserAwareAction
    val action = silhouette.UserAwareAction.async(bodyParsers.empty) { r =>
      if (canAccessRoute(request.path, r.identity)) next(request)
      else
        Future.successful(Unauthorized(unauthorizedMessage))
    }

    action(request).run
  }
}
