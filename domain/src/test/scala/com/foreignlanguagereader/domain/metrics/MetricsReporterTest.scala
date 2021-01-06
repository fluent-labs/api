package com.foreignlanguagereader.domain.metrics

import org.scalatest.funspec.AsyncFunSpec

class MetricsReporterTest extends AsyncFunSpec {

  describe("A metrics reporter") {
    it("can initialize without error") {
      val metricsReporter = new MetricsReporter()
      succeed
    }
  }
}
