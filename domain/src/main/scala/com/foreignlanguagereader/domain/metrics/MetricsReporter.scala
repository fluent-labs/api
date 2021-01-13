package com.foreignlanguagereader.domain.metrics

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.metrics.label.DatabaseMethod.DatabaseMethod
import com.foreignlanguagereader.domain.metrics.label.ElasticsearchMethod.ElasticsearchMethod
import com.foreignlanguagereader.domain.metrics.label.RequestPath.RequestPath
import com.foreignlanguagereader.domain.metrics.label.WebsterDictionary.WebsterDictionary
import io.prometheus.client.Histogram
import io.prometheus.client.hotspot.DefaultExports
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class MetricsReporter @Inject() (holder: MetricHolder, config: Configuration) {
  // JVM metrics
  val getJvmMetrics: Boolean =
    config.getOptional[Boolean]("metrics.reportJVM").getOrElse(true)

  def initialize(): Unit = {
    if (getJvmMetrics) {
      DefaultExports.initialize()
    }
    holder.initializeMetricsToZero()
  }

  /*
   * End user RED metrics
   */

  // Requests and Duration
  def reportRequestStarted(
      method: String,
      path: RequestPath
  ): Option[Histogram.Timer] = {
    holder.inc(Metric.ACTIVE_REQUESTS)
    holder.report(Metric.REQUEST_COUNT, path.toString)
    holder.startTimer(
      Metric.REQUESTS_LATENCY_SECONDS,
      Seq(method, path.toString)
    )
  }
  def reportRequestFinished(timer: Option[Histogram.Timer]): Unit = {
    timer.map(_.observeDuration())
    holder.dec(Metric.ACTIVE_REQUESTS)
  }

  // Errors
  def reportRequestFailure(path: RequestPath): Unit =
    holder.report(Metric.REQUEST_FAILURES, path.toString)
  def reportBadRequest(path: RequestPath): Unit =
    holder.report(Metric.BAD_REQUEST_DATA, path.toString)

  /*
   * Downstream dependency RED metrics
   */

  // Database
  def reportDatabaseRequestStarted(
      method: DatabaseMethod
  ): Option[Histogram.Timer] = {
    holder.inc(Metric.ACTIVE_DATABASE_REQUESTS)
    holder.report(Metric.DATABASE_CALLS, method.toString)
    holder.startTimer(
      Metric.DATABASE_LATENCY_SECONDS,
      Seq(method.toString)
    )
  }
  def reportDatabaseRequestFinished(
      timer: Option[Histogram.Timer]
  ): Unit = {
    timer.map(_.observeDuration())
    holder.dec(Metric.ACTIVE_DATABASE_REQUESTS)
  }
  def reportDatabaseFailure(
      timer: Option[Histogram.Timer],
      method: DatabaseMethod
  ): Unit = {
    timer.map(_.observeDuration())
    holder.report(Metric.DATABASE_FAILURES, method.toString)
  }

  // Elasticsearch
  def reportElasticsearchRequestStarted(
      method: ElasticsearchMethod
  ): Option[Histogram.Timer] = {
    holder.inc(Metric.ACTIVE_ELASTICSEARCH_REQUESTS)
    holder.report(Metric.ELASTICSEARCH_CALLS, method.toString)
    holder.startTimer(
      Metric.ELASTICSEARCH_LATENCY_SECONDS,
      Seq(method.toString)
    )
  }
  def reportElasticsearchRequestFinished(
      timer: Option[Histogram.Timer]
  ): Unit = {
    timer.map(_.observeDuration())
    holder.dec(Metric.ACTIVE_ELASTICSEARCH_REQUESTS)
  }
  def reportElasticsearchFailure(
      timer: Option[Histogram.Timer],
      method: ElasticsearchMethod
  ): Unit = {
    timer.map(_.observeDuration())
    holder.report(Metric.ELASTICSEARCH_FAILURES, method.toString)
  }

  // Google
  def reportGoogleRequestStarted(): Option[Histogram.Timer] = {
    holder.inc(Metric.ACTIVE_GOOGLE_REQUESTS)
    holder.report(Metric.GOOGLE_CALLS)
    holder.startTimer(
      Metric.GOOGLE_LATENCY_SECONDS,
      Seq("get_tokens")
    )
  }
  def reportGoogleRequestFinished(timer: Option[Histogram.Timer]): Unit = {
    timer.map(_.observeDuration())
    holder.dec(Metric.ACTIVE_GOOGLE_REQUESTS)
  }

  def reportGoogleFailure(timer: Option[Histogram.Timer]): Unit = {
    timer.map(_.observeDuration())
    holder.report(Metric.GOOGLE_FAILURES)
  }

  // Language service
  def reportLanguageServiceRequestStarted(
      language: Language
  ): Option[Histogram.Timer] = {
    holder.inc(Metric.ACTIVE_LANGUAGE_SERVICE_REQUESTS)
    holder.report(Metric.LANGUAGE_SERVICE_CALLS, language.toString)
    holder.startTimer(
      Metric.LANGUAGE_SERVICE_LATENCY_SECONDS,
      Seq(language.toString)
    )
  }
  def reportLanguageServiceRequestFinished(
      timer: Option[Histogram.Timer]
  ): Unit = {
    timer.map(_.observeDuration())
    holder.dec(Metric.ACTIVE_LANGUAGE_SERVICE_REQUESTS)
  }

  def reportLanguageServiceFailure(
      timer: Option[Histogram.Timer],
      language: Language
  ): Unit = {
    timer.map(_.observeDuration())
    holder.report(Metric.LANGUAGE_SERVICE_FAILURES, language.toString)
  }

  // Webster
  def reportWebsterRequestStarted(
      dictionary: WebsterDictionary
  ): Option[Histogram.Timer] = {
    holder.inc(Metric.ACTIVE_WEBSTER_REQUESTS)
    holder.report(Metric.WEBSTER_CALLS, dictionary.toString)
    holder.startTimer(
      Metric.WEBSTER_LATENCY_SECONDS,
      Seq(dictionary.toString)
    )
  }
  def reportWebsterRequestFinished(timer: Option[Histogram.Timer]): Unit = {
    timer.map(_.observeDuration())
    holder.dec(Metric.ACTIVE_WEBSTER_REQUESTS)
  }

  def reportWebsterFailure(
      timer: Option[Histogram.Timer],
      dictionary: WebsterDictionary
  ): Unit = {
    timer.map(_.observeDuration())
    holder.report(Metric.WEBSTER_FAILURES, dictionary.toString)
  }

  /*
   * User behavior and experience metrics
   */
  def reportLearnerLanguage(
      learningLanguage: Language,
      baseLanguage: Language
  ): Unit = {
    holder.report(
      Metric.LEARNER_LANGUAGE_REQUESTS,
      Seq(learningLanguage.toString, baseLanguage.toString)
    )
  }
  def reportDefinitionsSearched(source: DefinitionSource): Unit = {
    holder.report(Metric.DEFINITIONS_SEARCHED, source.toString)
  }
  def reportDefinitionsNotFound(source: DefinitionSource): Unit = {
    holder.report(Metric.DEFINITIONS_NOT_FOUND, source.toString)
  }

  /*
   * Caching metrics
   */
  def reportDefinitionsSearchedInCache(source: DefinitionSource): Unit = {
    holder.report(Metric.DEFINITIONS_SEARCHED_IN_CACHE, source.toString)
  }
  def reportDefinitionsNotFoundInCache(source: DefinitionSource): Unit = {
    holder.report(Metric.DEFINITIONS_NOT_FOUND_IN_CACHE, source.toString)
  }

  initialize()
}
