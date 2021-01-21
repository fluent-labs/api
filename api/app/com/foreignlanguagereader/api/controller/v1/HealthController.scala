package com.foreignlanguagereader.api.controller.v1

import com.foreignlanguagereader.api.authentication.{
  AuthenticatedAction,
  AuthenticatedRequest
}
import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.database.DatabaseClient
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.dto.v1.health.{Readiness, ReadinessStatus}
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import java.io.StringWriter
import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class HealthController @Inject() (
    val controllerComponents: ControllerComponents,
    databaseClient: DatabaseClient,
    elasticsearchClient: ElasticsearchClient,
    websterClient: MirriamWebsterClient,
    authenticatedAction: AuthenticatedAction,
    implicit val ec: ExecutionContext
) extends BaseController {

  val logger: Logger = Logger(this.getClass)
  val metricsRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

  /*
   * This is simpler than the readiness check. It should just confirm that the server can respond to requests.
   */
  def health: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      logger.debug("Responding to health check: up")
      Ok(Json.obj("status" -> "up"))
    }

  /*
   * Indicates if instance is able to serve traffic. This should show if connections to downstream dependencies are healthy.
   */
  def readiness: Action[AnyContent] =
    Action.apply { implicit request: Request[AnyContent] =>
      {
        val status = Readiness(
          databaseClient.breaker.health(),
          elasticsearchClient.breaker.health(),
          websterClient.health()
        )
        val response = Json.toJson(status)
        logger.debug(s"Responding to readiness check check: $response")

        status.overall match {
          case ReadinessStatus.UP => Ok(response)
          case ReadinessStatus.DOWN =>
            ServiceUnavailable(response)
          case ReadinessStatus.DEGRADED => ImATeapot(response)
        }
      }
    }

  def getMetrics: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] => Ok(writeMetrics()) }

  def writeMetrics(): String = {
    val writer = new StringWriter()
    TextFormat.write004(
      writer,
      metricsRegistry.metricFamilySamples()
    )
    writer.toString
  }

  def checkAuthentication: Action[AnyContent] =
    authenticatedAction((request: AuthenticatedRequest[AnyContent]) => {
      val claim = request.jwt
      val token = Map[String, String](
        "Audience" -> claim.audience.getOrElse(Set()).mkString(", "),
        "Content" -> claim.content,
        "Expiration" -> claim.expiration.getOrElse(0).toString,
        "Issuer" -> claim.issuer.getOrElse("Unknown issuer"),
        "Subject" -> claim.subject.getOrElse("Unknown subject")
      )
      Ok(Json.toJson(token))
    })
}
