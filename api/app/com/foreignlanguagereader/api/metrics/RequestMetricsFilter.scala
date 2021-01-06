package com.foreignlanguagereader.api.metrics

import akka.stream.Materializer
import com.foreignlanguagereader.domain.metrics.{Metric, MetricsReporter}
import play.api.mvc.{Filter, RequestHeader, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// Class to time all requests
class RequestMetricsFilter @Inject() (implicit
    val mat: Materializer,
    metrics: MetricsReporter,
    executionContext: ExecutionContext
) extends Filter {
  val ignoredPaths = Set("health", "metrics", "readiness")

  def apply(
      nextFilter: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] = {
    val pathLabel =
      ApiMetricReporter.getLabelFromPath(requestHeader.path)
    val future = nextFilter(requestHeader)

    if (ignoredPaths.contains(pathLabel)) future
    else annotateWithMetrics(future, requestHeader.method, pathLabel)
  }

  def annotateWithMetrics(
      future: Future[Result],
      method: String,
      pathLabel: String
  ): Future[Result] = {
    metrics.report(Metric.REQUEST_COUNT, pathLabel)
    metrics.inc(Metric.ACTIVE_REQUESTS)
    val timer = metrics.requestTimer.map(
      _.labels(
        method,
        pathLabel
      ).startTimer()
    )
    future.onComplete(_ => {
      timer.map(_.observeDuration())
      metrics.dec(Metric.ACTIVE_REQUESTS)
    })
    future
  }
}
