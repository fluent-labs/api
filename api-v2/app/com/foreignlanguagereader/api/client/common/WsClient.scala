package com.foreignlanguagereader.api.client.common

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

/**
  * Common behavior for rest clients that we implement using WS
  */
trait WsClient extends Circuitbreaker {
  val ws: WSClient
  implicit val ec: ExecutionContext
  val headers: List[(String, String)] = List()

  override val logger: Logger = Logger(this.getClass)

  def get[T: ClassTag](
    url: String,
    isFailure: Throwable => Boolean = _ => true
  )(implicit reads: Reads[T]): Future[CircuitBreakerResult[Option[T]]] = {
    logger.info(s"Calling url $url")
    withBreaker(
      ws.url(url)
        // Doubled so that the circuit breaker will handle it.
        .withRequestTimeout(timeout * 2)
        .withHttpHeaders(headers: _*)
        .get()
        .map(response => {
          response.json.validate[T] match {
            case JsSuccess(result, _) => result
            case JsError(errors) =>
              val typeName = implicitly[ClassTag[T]].runtimeClass.getSimpleName
              val error = s"Failed to parse $typeName from $url: $errors"
              logger.error(error)
              throw new IllegalArgumentException(error)
          }
        }),
      isFailure
    ).map {
      // We are converting errors into Options here
      case Success(CircuitBreakerAttempt(value)) =>
        CircuitBreakerAttempt(Some(value))
      case Success(CircuitBreakerNonAttempt()) =>
        CircuitBreakerNonAttempt()
      case Failure(e) =>
        val typeName = implicitly[ClassTag[T]].runtimeClass.getSimpleName
        logger
          .error(s"Failed to get $typeName from $url: ${e.getMessage}", e)
        CircuitBreakerAttempt(None)
    }
  }
}
