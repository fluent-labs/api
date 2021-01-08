package com.foreignlanguagereader.domain.metrics

import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

class MetricsHolderTest extends AsyncFunSpec with MockitoSugar {

  describe("A metrics holder") {
    val holder = new MetricHolder()

    it("creates a counter for every metric") {
      val initializedMetrics =
        holder.unlabeledCounters.keySet ++ holder.labeledCounters.keySet ++ holder.gauges.keySet ++ holder.timers.keySet

      Metric.values.foreach(metric => {
        assert(initializedMetrics.contains(metric))
      })
      succeed
    }

  }
}
