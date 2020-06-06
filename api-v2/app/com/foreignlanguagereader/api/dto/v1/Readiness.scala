package com.foreignlanguagereader.api.dto.v1

import com.foreignlanguagereader.api.ReadinessStatus.ReadinessStatus
import play.api.libs.json.{Format, Json}

/**
  *
  * @param overall  Overall service status
  * @param database Whether service can connect to database to get user data
  * @param content  Whether service can connect to elasticsearch to get language content
  */
case class Readiness(overall: ReadinessStatus,
                     database: ReadinessStatus,
                     content: ReadinessStatus,
                     languageService: ReadinessStatus)

object Readiness {
  // Allows readiness to be serialized to JSON
  implicit val format: Format[Readiness] = Json.format
}
