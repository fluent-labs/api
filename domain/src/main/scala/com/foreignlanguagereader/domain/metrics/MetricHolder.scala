package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.domain.metrics.Metric.Metric
import com.foreignlanguagereader.domain.metrics.label.{
  ElasticsearchMethod,
  RequestPath,
  WebsterDictionary
}
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
class MetricHolder {
  val unlabeledCounters: Map[Metric, Counter] =
    MetricHolder.initializeUnlabeledMetric(
      List(Metric.GOOGLE_CALLS, Metric.GOOGLE_FAILURES)
    )(MetricHolder.buildCounter)

  val labeledCounters: Map[Metric, Counter] =
    MetricHolder.initializeLabeledMetric(
      Map(
        Metric.ELASTICSEARCH_CALLS -> List("method"),
        Metric.ELASTICSEARCH_FAILURES -> List("method"),
        Metric.WEBSTER_CALLS -> List("dictionary"),
        Metric.WEBSTER_FAILURES -> List("dictionary"),
        Metric.REQUEST_COUNT -> List("route"),
        Metric.REQUEST_FAILURES -> List("route"),
        Metric.BAD_REQUEST_DATA -> List("route"),
        Metric.LEARNER_LANGUAGE_REQUESTS -> List(
          "learningLanguage",
          "baseLanguage"
        ),
        Metric.DEFINITIONS_SEARCHED -> List("source"),
        Metric.DEFINITIONS_NOT_FOUND -> List("source"),
        Metric.DEFINITIONS_SEARCHED_IN_CACHE -> List("source"),
        Metric.DEFINITIONS_NOT_FOUND_IN_CACHE -> List("source")
      )
    )(MetricHolder.buildCounter)

  val gauges: Map[Metric, Gauge] =
    MetricHolder.initializeUnlabeledMetric(
      List(
        Metric.ACTIVE_REQUESTS,
        Metric.ACTIVE_ELASTICSEARCH_REQUESTS,
        Metric.ACTIVE_GOOGLE_REQUESTS,
        Metric.ACTIVE_WEBSTER_REQUESTS
      )
    )(
      MetricHolder.buildGauge
    )

  val timers: Map[Metric, Histogram] =
    MetricHolder.initializeLabeledMetric(
      Map(
        Metric.REQUESTS_LATENCY_SECONDS -> List("method", "path"),
        Metric.ELASTICSEARCH_LATENCY_SECONDS -> List("action"),
        Metric.GOOGLE_LATENCY_SECONDS -> List("api"),
        Metric.WEBSTER_LATENCY_SECONDS -> List("dictionary")
      )
    )(MetricHolder.buildTimer)

  def report(metric: Metric): Unit =
    unlabeledCounters.get(metric).foreach(_.inc())
  def report(metric: Metric, labels: Seq[String]): Unit =
    labeledCounters.get(metric).foreach(_.labels(labels: _*).inc())
  def report(metric: Metric, label: String): Unit = report(metric, List(label))

  def inc(metric: Metric): Unit =
    gauges.get(metric).foreach(_.inc())
  def dec(metric: Metric): Unit =
    gauges.get(metric).foreach(_.dec())

  def startTimer(
      metric: Metric,
      labels: Seq[String]
  ): Option[Histogram.Timer] = {
    timers.get(metric).map(timer => timer.labels(labels: _*).startTimer())
  }
}

object MetricHolder {
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

  def initializeLabeledMetric[T](metrics: Map[Metric, Seq[String]])(
      builder: (Metric, Seq[String]) => T
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
      labelNames: Seq[String]
  ): Counter =
    makeCounterBuilder(metric)
      .labelNames(labelNames: _*)
      .register()

  def buildGauge(
      metric: Metric
  ): Gauge =
    Gauge
      .build()
      .name(namespace + metric.toString.toLowerCase)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
      .register()

  def buildTimer(metric: Metric, labelNames: Seq[String]): Histogram =
    Histogram
      .build()
      .name(namespace + metric.toString)
      .help(metric.toString.toLowerCase.replaceAll("_", " "))
      .labelNames(labelNames: _*)
      .register()

}
