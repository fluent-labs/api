package com.foreignlanguagereader.api.controller.v1

import java.util.concurrent.TimeUnit

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.dto.v1
import com.foreignlanguagereader.api.dto.v1.{
  Readiness,
  ReadinessService,
  ReadinessStatus
}
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

@Singleton
class HealthController @Inject()(val controllerComponents: ControllerComponents,
                                 elasticsearchClient: ElasticsearchClient,
                                 languageServiceClient: LanguageServiceClient,
                                 implicit val ec: ExecutionContext)
    extends BaseController {

  val timeout = Duration(1, TimeUnit.SECONDS)

  /*
   * This is simpler than the readiness check. It should just confirm that the server can respond to requests.
   */
  def health: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(Json.obj("status" -> "up"))
  }

  /*
   * Indicates if instance is able to serve traffic. This should:
   * - Check connection to DB
   * - Check connection to Elasticsearch
   * But for now a static response is fine
   */
  def readiness: Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      // Trigger all the requests in parallel
      val database = ReadinessStatus.UP
      val elasticsearchFuture = Future {
        elasticsearchClient.checkConnection(timeout)
      }
      val languageServiceFuture = languageServiceClient
        .checkConnection(timeout)

      // Block until all the results are back or timed out
      val status = for {
        elasticsearch <- elasticsearchFuture
        languageService <- languageServiceFuture
      } yield Readiness(database, elasticsearch, languageService)

      status.map { status =>
        {
          val response = Json.toJson(status)

          status.overall match {
            case ReadinessStatus.UP => Ok(response)
            case ReadinessStatus.DOWN =>
              ServiceUnavailable(response)
            case ReadinessStatus.DEGRADED => ImATeapot(response)
          }
        }
      }
  }
}
