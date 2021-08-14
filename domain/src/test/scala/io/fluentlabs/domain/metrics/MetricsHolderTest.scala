package io.fluentlabs.domain.metrics

import io.fluentlabs.content.types.internal.definition.DefinitionSource
import Metric.Metric
import com.foreignlanguagereader.domain.metrics.label.{
  ElasticsearchMethod,
  RequestPath,
  WebsterDictionary
}
import io.fluentlabs.domain.metrics.label.{
  ElasticsearchMethod,
  RequestPath,
  WebsterDictionary
}
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

class MetricsHolderTest extends AsyncFunSpec with MockitoSugar {

  def testMetricValues(metric: Metric, labels: Set[String])(implicit
      holder: MetricHolder
  ): Unit = {
    labels.foreach(label => {
      val count = holder.labeledCounters
        .get(metric)
        .map(_.labels(label).get())
        .getOrElse(-1)
      assert(count == 0, s"Metric $metric, $label was not zero")
    })
  }

  describe("A metrics holder") {
    implicit val holder: MetricHolder = new MetricHolder()

    it("creates a counter for every metric") {
      val initializedMetrics =
        holder.unlabeledCounters.keySet ++ holder.labeledCounters.keySet ++ holder.gauges.keySet ++ holder.timers.keySet

      Metric.values.foreach(metric => {
        assert(initializedMetrics.contains(metric))
      })
      succeed
    }

    it("initializes every metric") {
      holder.initializeMetricsToZero()
      holder.unlabeledCounters.foreach { case (_, counter) =>
        assert(counter.get() == 0)
      }
      holder.gauges.foreach { case (_, gauge) =>
        assert(gauge.get() == 0)
      }

      testMetricValues(
        Metric.ELASTICSEARCH_CALLS,
        ElasticsearchMethod.values.map(_.toString)
      )
      testMetricValues(
        Metric.ELASTICSEARCH_FAILURES,
        ElasticsearchMethod.values.map(_.toString)
      )
      testMetricValues(
        Metric.WEBSTER_CALLS,
        WebsterDictionary.values.map(_.toString)
      )
      testMetricValues(
        Metric.WEBSTER_FAILURES,
        WebsterDictionary.values.map(_.toString)
      )
      testMetricValues(
        Metric.REQUEST_COUNT,
        RequestPath.values.map(_.toString)
      )
      testMetricValues(
        Metric.REQUEST_FAILURES,
        RequestPath.values.map(_.toString)
      )
      testMetricValues(
        Metric.BAD_REQUEST_DATA,
        RequestPath.values.map(_.toString)
      )
      testMetricValues(
        Metric.DEFINITIONS_SEARCHED,
        DefinitionSource.values.map(_.toString)
      )
      testMetricValues(
        Metric.DEFINITIONS_NOT_FOUND,
        DefinitionSource.values.map(_.toString)
      )
      testMetricValues(
        Metric.DEFINITIONS_SEARCHED_IN_CACHE,
        DefinitionSource.values.map(_.toString)
      )
      testMetricValues(
        Metric.DEFINITIONS_NOT_FOUND_IN_CACHE,
        DefinitionSource.values.map(_.toString)
      )

      succeed
    }
  }
}
