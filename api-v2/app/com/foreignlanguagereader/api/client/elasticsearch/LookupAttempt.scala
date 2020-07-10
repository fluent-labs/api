package com.foreignlanguagereader.api.client.elasticsearch

import play.api.libs.json.{Format, Json}

case class LookupAttempt(index: String,
                         fields: Seq[Tuple2[String, String]],
                         count: Int)
object LookupAttempt {
  implicit val format: Format[LookupAttempt] = Json.format[LookupAttempt]
}
