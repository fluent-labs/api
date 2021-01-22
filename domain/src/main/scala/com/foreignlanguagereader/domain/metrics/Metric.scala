package com.foreignlanguagereader.domain.metrics

object Metric extends Enumeration {
  type Metric = Value

  /*
   * End user RED metrics
   */

  // Requests
  val REQUEST_COUNT: Value = Value("request_count")
  val ACTIVE_REQUESTS: Value = Value("active_requests")

  // Errors
  val REQUEST_FAILURES: Value = Value("request_failures")
  val BAD_REQUEST_DATA: Value =
    Value("bad_request_data") // Likely indicates issues on the frontend
  val UNAUTHENTICATED_REQUEST: Value = Value("unauthenticated_request")
  val BAD_REQUEST_TOKEN: Value = Value("bad_request_token")

  // Duration
  val REQUESTS_LATENCY_SECONDS: Value = Value("requests_latency_seconds")

  /*
   * Downstream dependency RED metrics
   */

  // Requests
  val DATABASE_CALLS: Value = Value("database_calls")
  val ACTIVE_DATABASE_REQUESTS: Value = Value(
    "active_database_requests"
  )
  val ELASTICSEARCH_CALLS: Value = Value("elasticsearch_calls")
  val ACTIVE_ELASTICSEARCH_REQUESTS: Value = Value(
    "active_elasticsearch_requests"
  )
  val GOOGLE_CALLS: Value = Value("google_calls")
  val ACTIVE_GOOGLE_REQUESTS: Value = Value("active_google_requests")
  val LANGUAGE_SERVICE_CALLS: Value = Value("language_service_calls")
  val ACTIVE_LANGUAGE_SERVICE_REQUESTS: Value = Value(
    "active_language_service_requests"
  )
  val WEBSTER_CALLS: Value = Value("webster_calls")
  val ACTIVE_WEBSTER_REQUESTS: Value = Value("active_webster_requests")

  // Errors
  val DATABASE_FAILURES: Value = Value("database_failures")
  val ELASTICSEARCH_FAILURES: Value = Value("elasticsearch_failures")
  val GOOGLE_FAILURES: Value = Value("google_failures")
  val LANGUAGE_SERVICE_FAILURES: Value = Value("language_service_failures")
  val WEBSTER_FAILURES: Value = Value("webster_failures")

  // Duration
  val DATABASE_LATENCY_SECONDS: Value = Value("database_latency_seconds")
  val ELASTICSEARCH_LATENCY_SECONDS: Value = Value(
    "elasticsearch_latency_seconds"
  )
  val GOOGLE_LATENCY_SECONDS: Value = Value("google_latency_seconds")
  val LANGUAGE_SERVICE_LATENCY_SECONDS: Value = Value(
    "language_service_latency_seconds"
  )
  val WEBSTER_LATENCY_SECONDS: Value = Value("webster_latency_seconds")

  /*
   * User behavior metrics
   */

  val LEARNER_LANGUAGE_REQUESTS: Value = Value("learner_language_requests")
  val DEFINITIONS_SEARCHED: Value = Value("definitions_searched")

  /*
   * Caching metrics
   */
  val DEFINITIONS_NOT_FOUND: Value =
    Value("definitions_not_found")
  val DEFINITIONS_SEARCHED_IN_CACHE: Value =
    Value("definitions_searched_in_cache")
  val DEFINITIONS_NOT_FOUND_IN_CACHE: Value =
    Value("definitions_not_found_in_cache")
}
