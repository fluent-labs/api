package com.foreignlanguagereader.api.controller.v1

import com.foreignlanguagereader.api.ReadinessStatus
import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.dto.v1.Readiness
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class HealthController @Inject()(val controllerComponents: ControllerComponents,
                                 elasticsearchClient: ElasticsearchClient,
                                 languageServiceClient: LanguageServiceClient)
    extends BaseController {

  /*
   * This is simpler than the readiness check. It should just confirm that the server can respond to requests.
   */
  def health(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(Json.obj("status" -> "up"))
  }

  /*
   * Indicates if instance is able to serve traffic. This should:
   * - Check connection to DB
   * - Check connection to Elasticsearch
   * But for now a static response is fine
   */
  def readiness(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      val database = ReadinessStatus.UP
      val content = elasticsearchClient.checkConnection
      val languageService = languageServiceClient.checkConnection
      val statuses = List(database, content, languageService)

      val overallStatus =
        if (statuses.forall(_.eq(ReadinessStatus.UP))) ReadinessStatus.UP
        else if (statuses.forall(_.eq(ReadinessStatus.DOWN)))
          ReadinessStatus.DOWN
        else ReadinessStatus.DEGRADED

      val response = Json.toJson(
        Readiness(overallStatus, database, content, languageService)
      )

      overallStatus match {
        case ReadinessStatus.UP       => Ok(response)
        case ReadinessStatus.DOWN     => ServiceUnavailable(response)
        case ReadinessStatus.DEGRADED => ImATeapot(response)
      }
  }
}
