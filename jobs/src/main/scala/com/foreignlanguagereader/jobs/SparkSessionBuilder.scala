package com.foreignlanguagereader.jobs

import org.apache.spark.sql.SparkSession

// $COVERAGE-OFF$
object SparkSessionBuilder {
  def build(name: String): SparkSession = {
    SparkSession.builder
      .appName(name)
      .config("es.nodes", "elastic.foreignlanguagereader.com")
      .config("es.index.auto.create", "true")
      .config("es.net.ssl", "true")
      .config("es.net.http.auth.user", sys.env("es_user"))
      .config("es.net.http.auth.pass", sys.env("es_password"))
      .getOrCreate()
  }
}
// $COVERAGE-ON$
