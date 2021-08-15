package io.fluentlabs.api.metrics

import akka.stream.Materializer
import io.fluentlabs.domain.metrics.MetricsReporter
import io.fluentlabs.domain.metrics.label.RequestPath.RequestPath
import io.fluentlabs.domain.metrics.label.RequestPath
import play.api.mvc.{Filter, RequestHeader, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// Class to time all requests
class RequestMetricsFilter @Inject() (implicit
    val mat: Materializer,
    metrics: MetricsReporter,
    executionContext: ExecutionContext
) extends Filter {
  val ignoredPaths =
    Set(RequestPath.HEALTH, RequestPath.METRICS, RequestPath.READINESS)

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
      pathLabel: RequestPath
  ): Future[Result] = {
    val timer = metrics.reportRequestStarted(method, pathLabel)
    future.onComplete(_ => {
      metrics.reportRequestFinished(timer)
    })
    future
  }
}
