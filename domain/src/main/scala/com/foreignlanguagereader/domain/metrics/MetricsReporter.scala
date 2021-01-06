package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.domain.metrics.Metric.Metric
import io.prometheus.client.hotspot.DefaultExports
import io.prometheus.client.{Counter, Gauge, Histogram}
import play.api.Logger

import javax.inject.Singleton
import scala.util.{Failure, Success, Try}

/*
 * Two motivations for this class
 * - Put an interface on metrics, in case we want to change solutions. Metrics design is still in an early stage
 * - Metrics are global and shared among all instances. Creating one twice causes an error.
 *      This makes creating metrics in classes play badly in tests, so we use this class that can be stubbed.
 */
@Singleton
class MetricsReporter {
  // JVM metrics
  DefaultExports.initialize()

  val unlabeledCounters: Map[Metric, Counter] =
    MetricsReporter.initializeUnlabeledMetric(
      List(Metric.GOOGLE_CALLS, Metric.GOOGLE_FAILURES)
    )(MetricsReporter.buildCounter)

  val labeledCounters: Map[Metric, Counter] =
    MetricsReporter.initializeLabeledMetric(
      Map(
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
      )
    )(MetricsReporter.buildCounter)

  val gauges: Map[Metric, Gauge] =
    MetricsReporter.initializeUnlabeledMetric(List(Metric.ACTIVE_REQUESTS))(
      MetricsReporter.buildGauge
    )

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

  def inc(metric: Metric): Unit =
    gauges.get(metric).foreach(_.inc())
  def dec(metric: Metric): Unit =
    gauges.get(metric).foreach(_.dec())

  // Initialize everything so metrics always show up
  unlabeledCounters.mapValues(_.inc(0))
  labeledCounters.mapValues(_.labels().inc(0))
  gauges.mapValues(_.inc(0))

  val requestTimer: Option[Histogram] =
    MetricsReporter.safelyMakeMetric(Metric.REQUESTS_LATENCY_SECONDS)(() =>
      Histogram
        .build()
        .name(
          MetricsReporter.namespace + Metric.REQUESTS_LATENCY_SECONDS.toString
        )
        .help("Request latency in seconds.")
        .labelNames("method", "path")
        .register()
    )

  def startTimer(method: String, path: String): Option[Histogram.Timer] = {
    requestTimer.map(
      _.labels(
        method,
        path
      ).startTimer()
    )
  }
}

object MetricsReporter {
  val namespace = "flr_"
  val logger: Logger = Logger(this.getClass)

  // Handle duplicate registry when running locally
  // Play automatic reload will break otherwise
  def safelyMakeMetric[T](metric: Metric)(method: () => T): Option[T] = {
    Try(method.apply()) match {
      case Success(metric) => Some(metric)
      case Failure(e) =>
        logger.error(
          s"Failed to initialize metric ${metric.toString.toLowerCase}",
          e
        )
        None
    }
  }

  def initializeUnlabeledMetric[T](metrics: List[Metric])(
      builder: Metric => T
  ): Map[Metric, T] = {
    metrics
      .map(metric => metric -> safelyMakeMetric(metric)(() => builder(metric)))
      .collect {
        case (metric, Some(counter)) => metric -> counter
      }
      .toMap
  }

  def initializeLabeledMetric[T](metrics: Map[Metric, String])(
      builder: (Metric, String) => T
  ): Map[Metric, T] = {
    metrics
      .map {
        case (metric, labels) =>
          metric -> safelyMakeMetric(metric)(() => builder(metric, labels))
      }
      .collect {
        case (metric, Some(counter)) => metric -> counter
      }
  }

  def makeCounterBuilder(metric: Metric): Counter.Builder = {
    Counter
      .build()
      .name(namespace + metric.toString.toLowerCase)
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

  def buildGauge(
      metric: Metric
  ): Gauge =
    Gauge
      .build()
      .name(namespace + metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
      .register()
}
