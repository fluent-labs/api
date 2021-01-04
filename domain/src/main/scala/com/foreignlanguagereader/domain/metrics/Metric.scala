package com.foreignlanguagereader.domain.metrics

// $COVERAGE-OFF$
object Metric extends Enumeration {
  type Metric = Value

  // Latency - Duration
  // Can this be attached as an interceptor?
  val REQUESTS_LATENCY_SECONDS: Value = Value("requests_latency_seconds")

  // Traffic - Rates
  // Can this be attached as an interceptor?
  val REQUEST_COUNT: Value = Value("request_count")

  // Errors
  val REQUEST_FAILURES: Value = Value("request_failures")
  val BAD_REQUEST_DATA: Value =
    Value("bad_request_data") // Likely indicates issues on the frontend

  // Clients
  val ELASTICSEARCH_CALLS: Value = Value("elasticsearch_calls")
  val ELASTICSEARCH_FAILURES: Value = Value("elasticsearch_failures")
  val GOOGLE_CALLS: Value = Value("google_calls")
  val GOOGLE_FAILURES: Value = Value("google_failures")
  val WEBSTER_CALLS: Value = Value("webster_calls")
  val WEBSTER_FAILURES: Value = Value("webster_failures")

  // Saturation
  val ACTIVE_REQUESTS: Value = Value("active_requests")

  // Usage metrics
  val CHINESE_LEARNER_REQUESTS: Value =
    Value("chinese_learner_requests") // label by definition language
  val ENGLISH_LEARNER_REQUESTS: Value =
    Value("english_learner_requests") // label by definition language
  val SPANISH_LEARNER_REQUESTS: Value =
    Value("spanish_learner_requests") // label by definition language

  // Caching results
  val DEFINITIONS_SEARCHED: Value =
    Value("definitions_searched") // label by language
  val DEFINITIONS_NOT_FOUND: Value =
    Value("definitions_not_found") // label by language
  val DEFINITIONS_SEARCHED_IN_CACHE: Value =
    Value("definitions_searched_in_cache") // label by source
  val DEFINITIONS_NOT_FOUND_IN_CACHE: Value =
    Value("definitions_not_found_in_cache") // label by source
}
// $COVERAGE-ON$
