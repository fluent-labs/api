package com.foreignlanguagereader.api.metrics

import io.fluentlabs.content.types.Language
import io.fluentlabs.domain.metrics.label.RequestPath.RequestPath
import io.fluentlabs.domain.metrics.label.RequestPath

import scala.util.matching.Regex

object ApiMetricReporter {
  val languageRegex: String = s"[${Language.values.mkString("|")}]+"
  val definitionRegex: Regex =
    s"/v1/language/definition/$languageRegex/[^/]+/?".r
  val definitionsRegex: Regex =
    s"/v1/language/definitions/$languageRegex/$languageRegex/?".r
  val documentRegex: Regex = s"/v1/language/document/$languageRegex/?".r

  def getLabelFromPath(path: String): RequestPath =
    path match {
      case definitionRegex()      => RequestPath.DEFINITION
      case definitionsRegex()     => RequestPath.DEFINITIONS
      case documentRegex()        => RequestPath.DOCUMENT
      case "/health"              => RequestPath.HEALTH
      case "/metrics"             => RequestPath.METRICS
      case "/readiness"           => RequestPath.READINESS
      case "/v1/vocabulary/words" => RequestPath.WORDS
      case _                      => RequestPath.UNKNOWN
    }
}
