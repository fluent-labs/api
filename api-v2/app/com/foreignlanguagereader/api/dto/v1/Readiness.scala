package com.foreignlanguagereader.api.dto.v1
import com.foreignlanguagereader.api.dto.v1.ReadinessStatus.ReadinessStatus
import play.api.libs.json._

/**
  *
  * @param database Whether service can connect to database to get user data
  * @param content  Whether service can connect to elasticsearch to get language content
  */
case class Readiness(database: ReadinessStatus,
                     content: ReadinessStatus,
                     languageService: ReadinessStatus) {
  def overall: ReadinessStatus = {
    val statuses = List(database, content, languageService)
    if (statuses.forall(_.eq(ReadinessStatus.UP))) ReadinessStatus.UP
    else if (statuses.forall(_.eq(ReadinessStatus.DOWN)))
      ReadinessStatus.DOWN
    else ReadinessStatus.DEGRADED
  }
}

object Readiness {
  // Allows readiness to be serialized to JSON
  implicit val format: Format[Readiness] = Json.format
}

object ReadinessStatus extends Enumeration {
  type ReadinessStatus = Value
  val UP, DOWN, DEGRADED = Value

  implicit val readinessStatusFormat: Format[ReadinessStatus] =
    new Format[ReadinessStatus] {
      def reads(json: JsValue) =
        JsSuccess(ReadinessStatus.withName(json.as[String]))
      def writes(status: ReadinessStatus.ReadinessStatus) =
        JsString(status.toString)
    }
}
