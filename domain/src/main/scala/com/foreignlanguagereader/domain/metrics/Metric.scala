package com.foreignlanguagereader.domain.metrics

object Metric extends Enumeration {
  type Metric = Value

  // Clients
  val ELASTICSEARCH_SUCCESS: Value = Value("elasticsearch_success")
  val ELASTICSEARCH_FAILURE: Value = Value("elasticsearch_failure")
  val GOOGLE_SUCCESS: Value = Value("google_success")
  val GOOGLE_FAILURE: Value = Value("google_failure")
  val WEBSTER_SUCCESS: Value = Value("webster_success")
  val WEBSTER_FAILURE: Value = Value("webster_failure")
}
