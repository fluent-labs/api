package com.foreignlanguagereader.domain.metrics

object Metric extends Enumeration {
  type Metric = Value

  // Latency - Duration
  val REQUESTS_LATENCY_SECONDS: Value = Value("requests_latency_seconds")

  // Traffic - Rates
  val REQUEST_SUCCESSES: Value = Value("request_successes")
  // Errors
  val REQUEST_FAILURES: Value = Value("request_failures")
  val BAD_REQUEST_DATA: Value = Value("bad_request_data")
  // Clients
  val ELASTICSEARCH_SUCCESSES: Value = Value("elasticsearch_successes")
  val ELASTICSEARCH_FAILURES: Value = Value("elasticsearch_failures")
  val GOOGLE_SUCCESSES: Value = Value("google_successes")
  val GOOGLE_FAILURES: Value = Value("google_failures")
  val WEBSTER_SUCCESSES: Value = Value("webster_successes")
  val WEBSTER_FAILURES: Value = Value("webster_failures")

  // Saturation
  val ACTIVE_REQUESTS: Value = Value("active_requests")
}
