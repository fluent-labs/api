package com.foreignlanguagereader.domain.client.spark

import org.apache.spark.sql.SparkSession

object NLPSparkContext {
  val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("NLPSpark")
    .getOrCreate
}
