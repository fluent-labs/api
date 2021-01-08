package com.foreignlanguagereader.api.metrics

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.domain.metrics.label.RequestPath
import com.foreignlanguagereader.domain.metrics.label.RequestPath.RequestPath

import scala.util.matching.Regex

object ApiMetricReporter {
  val languageRegex: String = s"[${Language.values.mkString("|")}]+"
  val definitionsRegex: Regex =
    s"/v1/language/definition/$languageRegex/[^/]+/?".r
  val documentRegex: Regex = s"/v1/language/definition/$languageRegex/?".r

  def getLabelFromPath(path: String): RequestPath =
    path match {
      case definitionsRegex() => RequestPath.DEFINITIONS
      case documentRegex()    => RequestPath.DOCUMENT
      case "/health"          => RequestPath.HEALTH
      case "/metrics"         => RequestPath.METRICS
      case "/readiness"       => RequestPath.READINESS
      case _                  => RequestPath.UNKNOWN
    }
}
