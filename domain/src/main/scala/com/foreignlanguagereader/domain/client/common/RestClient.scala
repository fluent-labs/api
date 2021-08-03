package com.foreignlanguagereader.domain.client.common

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerResult,
  Circuitbreaker
}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.ws.{BodyWritable, WSClient}

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/** Common behavior for rest clients that we implement using WS
  */
case class RestClient(
    ws: WSClient,
    implicit val ec: ExecutionContext,
    breaker: Circuitbreaker,
    name: String,
    headers: List[(String, String)],
    timeout: FiniteDuration
) {
  val logger: Logger = Logger(this.getClass)

  logger.info(s"Initialized ws client $name with timeout $timeout")

  def get[T: ClassTag](
      url: String,
      onError: Throwable => Unit
  )(implicit reads: Reads[T]): Future[CircuitBreakerResult[T]] = {
    logger.info(s"Calling url $url")
    val typeName = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    breaker.withBreaker(onError) {
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
    }
  }

  def post[T: BodyWritable, U](
      url: String,
      body: T,
      onError: Throwable => Unit
  )(implicit reads: Reads[U]): Future[CircuitBreakerResult[U]] = {
    logger.info(s"Calling url $url")
    breaker.withBreaker(onError) {
      ws.url(url)
        // Doubled so that the circuit breaker will handle it.
        .withRequestTimeout(timeout * 2)
        .withHttpHeaders(headers: _*)
        .post(body)
        .map(responseBody => {
          responseBody.json.validate[U] match {
            case JsSuccess(result, _) => result
            case JsError(errors) =>
              val error =
                s"Failed to parse response from $url: ${responseBody.body} errors: $errors"
              logger.error(error)
              throw new IllegalArgumentException(error)
          }
        })
    }
  }
}

class RestClientBuilder @Inject() (system: ActorSystem, ws: WSClient) {
  def buildClient(
      name: String,
      headers: List[(String, String)] = List(),
      timeout: FiniteDuration = FiniteDuration(60, TimeUnit.SECONDS),
      resetTimeout: FiniteDuration = FiniteDuration(60, TimeUnit.SECONDS),
      maxFailures: Int = 5
  )(implicit ec: ExecutionContext): RestClient = {
    val breaker: Circuitbreaker =
      new Circuitbreaker(system, ec, name, timeout, resetTimeout, maxFailures)
    RestClient(
      ws,
      ec,
      breaker,
      name,
      headers,
      timeout
    )
  }
}
