package io.fluentlabs.api.error

import play.api.libs.json.{Format, Json}

case class ServiceException(message: String)

object ServiceException {
  implicit val format: Format[ServiceException] =
    Json.format[ServiceException]
}
