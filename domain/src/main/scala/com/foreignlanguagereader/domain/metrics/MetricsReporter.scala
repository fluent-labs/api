package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.domain.metrics.Metric.Metric
import io.prometheus.client.{Counter, Gauge, Histogram}

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
    Metric.ELASTICSEARCH_SUCCESSES -> "method",
    Metric.ELASTICSEARCH_FAILURES -> "method",
    Metric.GOOGLE_SUCCESSES -> "api",
    Metric.GOOGLE_FAILURES -> "api",
    Metric.WEBSTER_SUCCESSES -> "dictionary",
    Metric.WEBSTER_FAILURES -> "dictionary",
    Metric.REQUEST_SUCCESSES -> "route",
    Metric.REQUEST_FAILURES -> "route",
    Metric.BAD_REQUEST_DATA -> "route"
  ).map {
    case (metric, labels) => metric -> buildCounter(metric, labels)
  }

  def report(metric: Metric): Unit = counters.get(metric).foreach(_.inc())
  def report(metric: Metric, label: String): Unit =
    counters.get(metric).foreach(_.labels(label).inc())

  def buildGauge(
      metric: Metric,
      labelNames: String
  ): Gauge =
    Gauge
      .build()
      .name(metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
      .labelNames(labelNames)
      .register()

  val gauges: Map[Metric, Gauge] = Map(Metric.ACTIVE_REQUESTS -> "route").map {
    case (metric, labels) => metric -> buildGauge(metric, labels)
  }

  def inc(metric: Metric): Unit =
    gauges.get(metric).foreach(_.inc())
  def dec(metric: Metric): Unit =
    gauges.get(metric).foreach(_.dec())

  val requestTimer: Histogram = Histogram
    .build()
    .name(Metric.REQUESTS_LATENCY_SECONDS.toString)
    .help("Request latency in seconds.")
    .labelNames("route")
    .register()
}
