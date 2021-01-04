package com.foreignlanguagereader.api.controller.v1

import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.domain.metrics.MetricsReporter
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
    elasticsearchClient: ElasticsearchClient,
    websterClient: MirriamWebsterClient,
    metrics: MetricsReporter,
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
   * Indicates if instance is able to serve traffic. This should:
   * - Check connection to DB
   * - Check connection to Elasticsearch
   * But for now a static response is fine
   */
  def readiness: Action[AnyContent] =
    Action.apply { implicit request: Request[AnyContent] =>
      metrics.requestTimer
        .labels("readiness")
        .time(() => {
          // Trigger all the requests in parallel
          val database = ReadinessStatus.UP

          val status = Readiness(
            database,
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
        })
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
}
