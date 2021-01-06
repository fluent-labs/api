package com.foreignlanguagereader.api.metrics

import com.foreignlanguagereader.content.types.Language

import scala.util.matching.Regex

object ApiMetricReporter {
  val languageRegex: String = s"[${Language.values.mkString("|")}]+"
  val definitionsRegex: Regex =
    "/v1/language/definition/$languageRegex/[^/]+/?".r

  def getLabelFromPath(path: String): String =
    path match {
      case definitionsRegex() => "definition"
      case "/health"          => "health"
      case "/metrics"         => "metrics"
      case "/readiness"       => "readiness"
      case _                  => path
    }
}
