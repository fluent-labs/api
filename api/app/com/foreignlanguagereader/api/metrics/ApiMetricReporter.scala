package com.foreignlanguagereader.api.metrics

import scala.util.matching.Regex

object ApiMetricReporter {
  val definitionsRegex: Regex = "/v1/language/definition/[A-Z]+/[^/]+[/]+".r

  def getLabelFromPath(path: String): String =
    path match {
      case definitionsRegex() => "definition"
      case "/health"          => "health"
      case "/metrics"         => "metrics"
      case "/readiness"       => "readiness"
      case _                  => path
    }
}
