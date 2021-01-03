package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.domain.metrics.Metric.Metric
import io.prometheus.client.Counter

import javax.inject.Singleton

@Singleton
class MetricsReporter {
  def buildCounter(
      metric: Metric,
      labelNames: String
  ): Counter =
    Counter
      .build()
      .name(metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
      .labelNames(labelNames)
      .register()

  val counters: Map[Metric, Counter] = Map(
    Metric.ELASTICSEARCH_SUCCESS -> "method",
    Metric.ELASTICSEARCH_FAILURE -> "method",
    Metric.GOOGLE_SUCCESS -> "api",
    Metric.GOOGLE_FAILURE -> "api",
    Metric.WEBSTER_SUCCESS -> "dictionary",
    Metric.WEBSTER_FAILURE -> "dictionary"
  ).map {
    case (metric, labels) => metric -> buildCounter(metric, labels)
  }

  def report(metric: Metric): Unit = counters.get(metric).foreach(_.inc())
  def report(metric: Metric, label: String): Unit =
    counters.get(metric).foreach(_.labels(label).inc())
}
