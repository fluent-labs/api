package com.foreignlanguagereader.jobs

import org.apache.spark.sql.SparkSession

object SparkSessionBuilder {
  def build(name: String): SparkSession = {
    SparkSession.builder
      .appName(name)
      .config("es.nodes", "content-es-http.content.svc.cluster.local")
      .config("es.net.ssl", "true")
      .config("es.net.ssl.cert.allow.self.signed", "true")
      .config(
        "es.net.ssl.truststore.location",
        "file:///etc/flrcredentials/spark_keystore.jks"
      )
      .config("es.net.ssl.truststore.pass", sys.env("es_truststore"))
      .config("es.net.http.auth.user", sys.env("es_user"))
      .config("es.net.http.auth.pass", sys.env("es_password"))
      .getOrCreate()
  }
}
