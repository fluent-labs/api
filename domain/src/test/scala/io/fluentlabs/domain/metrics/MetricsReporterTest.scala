package io.fluentlabs.domain.metrics

import org.mockito.ArgumentMatchers.{any, eq => MockitoEq}
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec
import play.api.{ConfigLoader, Configuration}

class MetricsReporterTest extends AsyncFunSpec with MockitoSugar {
  val holder: MetricHolder = mock[MetricHolder]
  val config: Configuration = mock[Configuration]

  describe("A metrics reporter") {
    it("can initialize without error") {
      when(
        config.getOptional[Boolean](
          MockitoEq("metrics.reportJVM")
        )(any[ConfigLoader[Boolean]])
      ).thenReturn(Some(true))

      val metricsReporter = new MetricsReporter(holder, config)
      succeed
    }
  }
}
