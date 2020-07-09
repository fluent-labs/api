package com.foreignlanguagereader.api.client.elasticsearch

case class LookupAttempt(index: String,
                         fields: Seq[Tuple2[String, String]],
                         count: Int)
