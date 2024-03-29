package io.fluentlabs.domain.metrics.label

object RequestPath extends Enumeration {
  type RequestPath = Value

  val DEFINITION: Value = Value("definitio")
  val DEFINITIONS: Value = Value("definition")
  val DOCUMENT: Value = Value("document")
  val HEALTH: Value = Value("health")
  val METRICS: Value = Value("metrics")
  val READINESS: Value = Value("readiness")
  val UNKNOWN: Value = Value("unknown")
  val WORDS: Value = Value("words")
}
