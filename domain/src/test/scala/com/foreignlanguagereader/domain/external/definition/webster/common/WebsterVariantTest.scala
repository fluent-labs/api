package com.foreignlanguagereader.domain.external.definition.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterVariantTest extends AnyFunSpec {
  describe("a variant") {
    val webster =
      "[{\"vl\":\"or less commonly\",\"va\":\"kab*ba*la\"},{\"vl\":\"or\",\"va\":\"ka*ba*la\"},{\"vl\":\"or\",\"va\":\"ca*ba*la\"},{\"vl\":\"or\",\"va\":\"cab*ba*la\"},{\"vl\":\"or\",\"va\":\"cab*ba*lah\"}]"
    val domain =
      "[{\"variant\":\"kab*ba*la\",\"variantLabel\":\"or less commonly\"},{\"variant\":\"ka*ba*la\",\"variantLabel\":\"or\"},{\"variant\":\"ca*ba*la\",\"variantLabel\":\"or\"},{\"variant\":\"cab*ba*la\",\"variantLabel\":\"or\"},{\"variant\":\"cab*ba*lah\",\"variantLabel\":\"or\"}]"

    it("can be read from JSON") {
      val variants = Json
        .parse(webster)
        .validate[List[WebsterVariant]](WebsterVariant.helper.readsList)
        .get
      assert(variants.size == 5)
      assert(variants(1).variant == "ka*ba*la")
      assert(variants(1).variantLabel.get == "or")
    }

    it("can be written back out to JSON") {
      val input = Json
        .parse(webster)
        .validate[List[WebsterVariant]](WebsterVariant.helper.readsList)
        .get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
