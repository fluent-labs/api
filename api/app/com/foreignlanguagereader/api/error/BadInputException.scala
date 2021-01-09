package com.foreignlanguagereader.api.error

import play.api.libs.json.{Format, Json}

case class BadInputException(message: String)

object BadInputException {
  implicit val format: Format[BadInputException] =
    Json.format[BadInputException]
}
