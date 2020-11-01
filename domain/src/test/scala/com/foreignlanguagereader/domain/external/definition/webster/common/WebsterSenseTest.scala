package com.foreignlanguagereader.domain.external.definition.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterSenseTest extends AnyFunSpec {
  describe("a sense") {
    val webster =
      "{\"sn\":\"1 a\",\"dt\":[[\"text\",\"{bc}to suddenly break open or come away from something often with a short, loud noise \"],[\"wsgram\",\"no obj\"],[\"vis\",[{\"t\":\"The balloon {it}popped{/it}. [={it}burst{/it}]\"},{\"t\":\"We heard the sound of corks {it}popping{/it} as the celebration began.\"},{\"t\":\"One of the buttons {it}popped{/it} off my sweater.\"}]],[\"wsgram\",\"+ obj\"],[\"vis\",[{\"t\":\"Don't {it}pop{/it} that balloon!\"},{\"t\":\"She {it}popped{/it} the cork on the champagne. [=she opened the bottle of champagne by removing the cork]\"}]]]}"
    val domain =
      "{\"definingText\":{\"text\":[\"{bc}to suddenly break open or come away from something often with a short, loud noise \"],\"examples\":[{\"text\":\"The balloon {it}popped{/it}. [={it}burst{/it}]\"},{\"text\":\"We heard the sound of corks {it}popping{/it} as the celebration began.\"},{\"text\":\"One of the buttons {it}popped{/it} off my sweater.\"},{\"text\":\"Don't {it}pop{/it} that balloon!\"},{\"text\":\"She {it}popped{/it} the cork on the champagne. [=she opened the bottle of champagne by removing the cork]\"}]}}"

    it("can be read from JSON") {
      val sense = Json
        .parse(webster)
        .validate[WebsterSense]
        .get
      assert(sense.definingText.examples.isDefined)
    }

    it("can be written back out to JSON") {
      val input = Json
        .parse(webster)
        .validate[WebsterSense]
        .get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }

  describe("a subdivided sense") {
    val webster =
      "{\"dt\":[[\"text\",\"{bc}the origin of life from nonliving matter\"]],\"sdsense\":{\"sd\":\"specifically\",\"dt\":[[\"text\",\"{bc}a theory in the evolution of early life on earth {bc}organic molecules and subsequent simple life forms first originated from inorganic substances\"]]}}"
    val domain =
      "{\"definingText\":{\"text\":[\"{bc}the origin of life from nonliving matter\"]},\"dividedSense\":{\"senseDivider\":\"specifically\",\"definingText\":{\"text\":[\"{bc}a theory in the evolution of early life on earth {bc}organic molecules and subsequent simple life forms first originated from inorganic substances\"]}}}"

    it("can be read from JSON") {
      val variants = Json
        .parse(webster)
        .validate[WebsterSense]
        .get
      assert(variants.dividedSense.isDefined)
      val dividedSense = variants.dividedSense.get
      assert(dividedSense.senseDivider == "specifically")
    }

    it("can be written back out to JSON") {
      val input = Json
        .parse(webster)
        .validate[WebsterSense]
        .get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
