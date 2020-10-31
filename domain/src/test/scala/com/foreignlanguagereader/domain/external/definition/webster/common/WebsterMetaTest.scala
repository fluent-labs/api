package com.foreignlanguagereader.domain.external.definition.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterMetaTest extends AnyFunSpec {
  describe("a meta section") {
    val webster =
      "{\"id\":\"battle:2\",\"uuid\":\"6aaba1f1-f7ca-48ce-b801-f866b41b8988\",\"sort\":\"020100000\",\"src\":\"learners\",\"section\":\"alpha\",\"stems\":[\"batteler\",\"battelers\",\"battle\",\"battle it out\",\"battled\",\"battled it out\",\"battler\",\"battlers\",\"battles\",\"battles it out\",\"battling\",\"battling it out\"],\"offensive\":false}"
    val domain =
      "{\"id\":\"battle:2\",\"uuid\":\"6aaba1f1-f7ca-48ce-b801-f866b41b8988\",\"sort\":\"020100000\",\"source\":\"learners\",\"section\":\"alpha\",\"stems\":[\"batteler\",\"battelers\",\"battle\",\"battle it out\",\"battled\",\"battled it out\",\"battler\",\"battlers\",\"battles\",\"battles it out\",\"battling\",\"battling it out\"],\"offensive\":false}"

    it("can be read from JSON") {
      val meta = Json.parse(webster).validate[WebsterMeta].get
      assert(!meta.offensive)
    }

    it("can be written back out to JSON") {
      val input = Json.parse(webster).validate[WebsterMeta].get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
