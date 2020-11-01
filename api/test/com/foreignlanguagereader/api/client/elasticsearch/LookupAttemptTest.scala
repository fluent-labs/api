package com.foreignlanguagereader.api.client.elasticsearch

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class LookupAttemptTest extends AnyFunSpec {
  describe("a lookup attempt") {
    // We need to keep this constant so that we don't invalidate the old records of lookup attempts
    // If you change this make sure it doesn't break lookup for old versions still in elasticsearch
    it("is written to JSON the same way as before") {
      val lookup =
        LookupAttempt("test", Map("field" -> "value", "field2" -> "value2"), 42)
      assert(
        Json.toJson(lookup).toString() == "{\"index\":\"test\",\"fields\":{\"field\":\"value\",\"field2\":\"value2\"},\"count\":42}"
      )
    }
  }
}
