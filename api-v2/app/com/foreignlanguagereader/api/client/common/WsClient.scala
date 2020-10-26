package com.foreignlanguagereader.api.client.common

import cats.data.Nested
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

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
  )(implicit reads: Reads[T]): Nested[Future, CircuitBreakerResult, T] = {
    logger.info(s"Calling url $url")
    val typeName = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    withBreaker(
      s"Failed to get $typeName from $url",
      isFailure,
      defaultIsSuccess[T],
      ws.url(url)
        // Doubled so that the circuit breaker will handle it.
        .withRequestTimeout(timeout * 2)
        .withHttpHeaders(headers: _*)
        .get()
        .map(_.json.validate[T])
        .map {
          case JsSuccess(result, _) => result
          case JsError(errors) =>
            val error = s"Failed to parse $typeName from $url: $errors"
            logger.error(error)
            throw new IllegalArgumentException(error)
        }
    )
  }
}
