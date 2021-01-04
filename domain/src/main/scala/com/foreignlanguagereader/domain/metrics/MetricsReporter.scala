package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.domain.metrics.Metric.Metric
import io.prometheus.client.{Counter, Gauge, Histogram}

import java.util.concurrent.Callable
import javax.inject.Singleton

/*
 * Two motivations for this class
 * - Put an interface on metrics, in case we want to change solutions. Metrics design is still in an early stage
 * - Metrics are global and shared among all instances. Creating one twice causes an error.
 *      This makes creating metrics in classes play badly in tests, so we use this class that can be stubbed.
 */
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
    List(Metric.GOOGLE_CALLS, Metric.GOOGLE_FAILURES)
      .map(metric => metric -> buildCounter(metric))
      .toMap

  val labeledCounters: Map[Metric, Counter] = Map(
    Metric.ELASTICSEARCH_CALLS -> "method",
    Metric.ELASTICSEARCH_FAILURES -> "method",
    Metric.WEBSTER_CALLS -> "dictionary",
    Metric.WEBSTER_FAILURES -> "dictionary",
    Metric.REQUEST_COUNT -> "route",
    Metric.REQUEST_FAILURES -> "route",
    Metric.BAD_REQUEST_DATA -> "route",
    Metric.CHINESE_LEARNER_REQUESTS -> "definitionLanguage",
    Metric.ENGLISH_LEARNER_REQUESTS -> "definitionLanguage",
    Metric.SPANISH_LEARNER_REQUESTS -> "definitionLanguage"
  ).map {
    case (metric, labels) => metric -> buildCounter(metric, labels)
  }

  def report(metric: Metric): Unit =
    unlabeledCounters.get(metric).foreach(_.inc())
  def report(metric: Metric, label: String): Unit =
    labeledCounters.get(metric).foreach(_.labels(label).inc())

  // Specialized class to report user metrics
  def reportLanguageUsage(
      learningLanguage: Language,
      definitionLanguage: Language
  ): Unit = {
    val metric = learningLanguage match {
      case Language.CHINESE             => Metric.CHINESE_LEARNER_REQUESTS
      case Language.CHINESE_TRADITIONAL => Metric.CHINESE_LEARNER_REQUESTS
      case Language.ENGLISH             => Metric.ENGLISH_LEARNER_REQUESTS
      case Language.SPANISH             => Metric.SPANISH_LEARNER_REQUESTS
    }
    val label = definitionLanguage.toString.toLowerCase
    report(metric, label)
  }

  def buildGauge(
      metric: Metric
  ): Gauge =
    Gauge
      .build()
      .name(metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
      .register()

  val gauges: Map[Metric, Gauge] = List(Metric.ACTIVE_REQUESTS)
    .map(metric => metric -> buildGauge(metric))
    .toMap

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
