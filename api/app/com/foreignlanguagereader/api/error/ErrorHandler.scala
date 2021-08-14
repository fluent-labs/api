package com.foreignlanguagereader.api.error

import com.foreignlanguagereader.api.metrics.ApiMetricReporter
import io.fluentlabs.domain.metrics.MetricsReporter
import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results.{InternalServerError, Status}
import play.api.mvc.{RequestHeader, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/*
 * Centralizes logging and metrics reporting in the event of an error.
 */
@Singleton
class ErrorHandler @Inject() (metrics: MetricsReporter)
    extends HttpErrorHandler {
  val logger: Logger = Logger(this.getClass)

  def onClientError(
      request: RequestHeader,
      statusCode: Int,
      message: String
  ): Future[Result] = {
    logger.error(
      s"Bad input provided from client on method ${request.method} path ${request.path}: $message"
    )

    metrics.reportBadRequest(ApiMetricReporter.getLabelFromPath(request.path))

    Future.successful(
      Status(statusCode)(
        Json.stringify(Json.toJson(BadInputException(message)))
      )
    )
  }

  def onServerError(
      request: RequestHeader,
      exception: Throwable
  ): Future[Result] = {
    logger.error(
      s"Internal Server Error on method ${request.method} path ${request.path}: ${exception.getMessage}",
      exception
    )

    metrics.reportRequestFailure(
      ApiMetricReporter.getLabelFromPath(request.path)
    )

    Future.successful(
      InternalServerError(
        Json.stringify(Json.toJson(ServiceException(exception.getMessage)))
      )
    )
  }
}
