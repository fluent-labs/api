package com.foreignlanguagereader.api.contentsource.webster.common

import com.foreignlanguagereader.api.contentsource.definition.webster.common.WebsterInflection
import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterInflectionTest extends AnyFunSpec {
  describe("an inflection section") {
    val webster =
      "[{\"ifc\":\"-seled\",\"if\":\"tas*seled\"},{\"il\":\"or\",\"ifc\":\"-selled\",\"if\":\"tas*selled\"},{\"ifc\":\"-sel*ing\",\"if\":\"tas*sel*ing\"},{\"il\":\"or\",\"ifc\":\"-sel*ling\",\"if\":\"tas*sel*ling\",\"prs\":[{\"mw\":\"\\u02c8ta-s(\\u0259-)li\\u014b\",\"sound\":{\"audio\":\"tassel02\",\"ref\":\"c\",\"stat\":\"1\"}}]}]"
    val domain =
      "[{\"inflection\":\"tas*seled\",\"inflectionCutback\":\"-seled\"},{\"inflection\":\"tas*selled\",\"inflectionCutback\":\"-selled\",\"inflectionLabel\":\"or\"},{\"inflection\":\"tas*sel*ing\",\"inflectionCutback\":\"-sel*ing\"},{\"inflection\":\"tas*sel*ling\",\"inflectionCutback\":\"-sel*ling\",\"inflectionLabel\":\"or\",\"pronunciation\":{}}]"

    it("can be read from JSON") {
      val inflection = Json
        .parse(webster)
        .validate[Seq[WebsterInflection]](WebsterInflection.helper.readsSeq)
        .get
      assert(inflection.size == 4)
      assert(inflection(0).inflection.get == "tas*seled")
    }

    it("can be written back out to JSON") {
      val input = Json
        .parse(webster)
        .validate[Seq[WebsterInflection]](WebsterInflection.helper.readsSeq)
        .get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
