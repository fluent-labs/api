package io.fluentlabs.domain.metrics

import io.fluentlabs.content.types.Language
import Metric.Metric
import io.fluentlabs.domain.metrics.label.{
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
      List(
        Metric.GOOGLE_CALLS,
        Metric.GOOGLE_FAILURES
      )
    )(MetricHolder.buildCounter)

  val labeledCounters: Map[Metric, Counter] =
    MetricHolder.initializeLabeledMetric(
      Map(
        Metric.DATABASE_CALLS -> List("method"),
        Metric.DATABASE_FAILURES -> List("method"),
        Metric.ELASTICSEARCH_CALLS -> List("method"),
        Metric.ELASTICSEARCH_FAILURES -> List("method"),
        Metric.LANGUAGE_SERVICE_CALLS -> List("language"),
        Metric.LANGUAGE_SERVICE_FAILURES -> List("language"),
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
        Metric.DEFINITIONS_NOT_FOUND_IN_CACHE -> List("source"),
        Metric.UNAUTHENTICATED_REQUEST -> List("route"),
        Metric.BAD_REQUEST_TOKEN -> List("route")
      )
    )(MetricHolder.buildCounter)

  val gauges: Map[Metric, Gauge] =
    MetricHolder.initializeUnlabeledMetric(
      List(
        Metric.ACTIVE_REQUESTS,
        Metric.ACTIVE_DATABASE_REQUESTS,
        Metric.ACTIVE_ELASTICSEARCH_REQUESTS,
        Metric.ACTIVE_GOOGLE_REQUESTS,
        Metric.ACTIVE_LANGUAGE_SERVICE_REQUESTS,
        Metric.ACTIVE_WEBSTER_REQUESTS
      )
    )(
      MetricHolder.buildGauge
    )

  val timers: Map[Metric, Histogram] =
    MetricHolder.initializeLabeledMetric(
      Map(
        Metric.REQUESTS_LATENCY_SECONDS -> List("method", "path"),
        Metric.DATABASE_LATENCY_SECONDS -> List("query"),
        Metric.ELASTICSEARCH_LATENCY_SECONDS -> List("action"),
        Metric.GOOGLE_LATENCY_SECONDS -> List("api"),
        Metric.LANGUAGE_SERVICE_LATENCY_SECONDS -> List("language"),
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

  // Initialize everything so metrics always show up
  def initializeMetricsToZero(): Unit = {
    unlabeledCounters.view.mapValues(_.inc(0))
    gauges.view.mapValues(_.inc(0))

    initializeLabeledCounter(
      Metric.ELASTICSEARCH_CALLS,
      ElasticsearchMethod.values.map(_.toString)
    )
    initializeLabeledCounter(
      Metric.ELASTICSEARCH_FAILURES,
      ElasticsearchMethod.values.map(_.toString)
    )

    initializeLabeledCounter(
      Metric.WEBSTER_CALLS,
      WebsterDictionary.values.map(_.toString)
    )
    initializeLabeledCounter(
      Metric.WEBSTER_FAILURES,
      WebsterDictionary.values.map(_.toString)
    )

    initializeLabeledCounter(
      Metric.REQUEST_COUNT,
      RequestPath.values.map(_.toString)
    )
    initializeLabeledCounter(
      Metric.REQUEST_FAILURES,
      RequestPath.values.map(_.toString)
    )
    initializeLabeledCounter(
      Metric.BAD_REQUEST_DATA,
      RequestPath.values.map(_.toString)
    )

    val languageLabels: Set[List[String]] = for {
      learningLanguage <- Language.values.map(_.toString).unsorted
      baseLanguage <- Language.values.map(_.toString).unsorted
    } yield List(learningLanguage, baseLanguage)
    initializeLabeledCounter(
      Metric.LEARNER_LANGUAGE_REQUESTS,
      languageLabels.toSeq
    )
  }

  def initializeLabeledCounter(
      metric: Metric,
      labels: Set[String]
  ): Unit =
    initializeLabeledCounter(
      metric,
      labels.map(label => List(label)).toSeq
    )

  def initializeLabeledCounter(
      metric: Metric,
      labels: Seq[Seq[String]]
  ): Unit = {
    labeledCounters
      .get(metric)
      .foreach(counter =>
        labels.foreach(labelSet => counter.labels(labelSet: _*).inc(0))
      )
  }
}

object MetricHolder {
  val namespace = "fluentlabs_api_"
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
      .collect { case (metric, Some(counter)) =>
        metric -> counter
      }
      .toMap
  }

  def initializeLabeledMetric[T](metrics: Map[Metric, Seq[String]])(
      builder: (Metric, Seq[String]) => T
  ): Map[Metric, T] = {
    metrics
      .map { case (metric, labels) =>
        metric -> safelyMakeMetric(metric)(() => builder(metric, labels))
      }
      .collect { case (metric, Some(counter)) =>
        metric -> counter
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
