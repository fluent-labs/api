package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.domain.metrics.Metric.Metric
import io.prometheus.client.{Counter, Gauge, Histogram}

import java.util.concurrent.Callable
import javax.inject.Singleton

@Singleton
class MetricsReporter {
  def makeCounterBuilder(metric: Metric): Counter.Builder = {
    Counter
      .build()
      .name(metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
  }

  def buildCounter(
      metric: Metric
  ): Counter =
    makeCounterBuilder(metric)
      .register()

  def buildCounter(
      metric: Metric,
      labelNames: String
  ): Counter =
    makeCounterBuilder(metric)
      .labelNames(labelNames)
      .register()

  val unlabeledCounters: Map[Metric, Counter] =
    List(Metric.GOOGLE_SUCCESSES, Metric.GOOGLE_FAILURES)
      .map(metric => metric -> buildCounter(metric))
      .toMap

  val labeledCounters: Map[Metric, Counter] = Map(
    Metric.ELASTICSEARCH_SUCCESSES -> "method",
    Metric.ELASTICSEARCH_FAILURES -> "method",
    Metric.WEBSTER_SUCCESSES -> "dictionary",
    Metric.WEBSTER_FAILURES -> "dictionary",
    Metric.REQUEST_SUCCESSES -> "route",
    Metric.REQUEST_FAILURES -> "route",
    Metric.BAD_REQUEST_DATA -> "route"
  ).map {
    case (metric, labels) => metric -> buildCounter(metric, labels)
  }

  def report(metric: Metric): Unit =
    unlabeledCounters.get(metric).foreach(_.inc())
  def report(metric: Metric, label: String): Unit =
    labeledCounters.get(metric).foreach(_.labels(label).inc())

  def buildGauge(
      metric: Metric,
      labelNames: String
  ): Gauge =
    Gauge
      .build()
      .name(metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
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

  def timeRequest[T](label: String)(request: Callable[T]): T = {
    requestTimer.labels(label).time(request)
  }
}
