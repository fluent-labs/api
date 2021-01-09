package com.foreignlanguagereader.domain.metrics.label

object ElasticsearchMethod extends Enumeration {
  type ElasticsearchMethod = Value

  val BULK: Value = Value("bulk")
  val INDEX: Value = Value("index")
  val MULTISEARCH: Value = Value("multisearch")
  val SEARCH: Value = Value("search")
}
