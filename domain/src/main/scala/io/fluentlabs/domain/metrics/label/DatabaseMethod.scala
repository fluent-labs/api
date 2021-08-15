package io.fluentlabs.domain.metrics.label

object DatabaseMethod extends Enumeration {
  type DatabaseMethod = Value

  val SETUP: Value = Value("setup")
  val INSERT: Value = Value("insert")
  val QUERY: Value = Value("query")
}
