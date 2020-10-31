package com.foreignlanguagereader.domain.external.definition.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterDefinedRunOnPhraseTest extends AnyFunSpec {
  describe("a definition") {
    val webster =
      "{\"drp\":\"abide by\",\"def\":[{\"sseq\":[[[\"sense\",{\"sn\":\"1\",\"dt\":[[\"text\",\"{bc}to conform to \"],[\"vis\",[{\"t\":\"{it}abide by{\\/it} the rules\"}]]]}]],[[\"sense\",{\"sn\":\"2\",\"dt\":[[\"text\",\"{bc}to accept without objection {bc}to {d_link|acquiesce|acquiesce} in \"],[\"vis\",[{\"t\":\"will {it}abide by{\\/it} your decision\"}]]]}]]]}]}"
    val domain =
      "{\"definedRunOnPhrase\":\"abide by\",\"definition\":[{\"senseSequence\":[[{\"definingText\":{\"text\":[\"{bc}to conform to \"],\"examples\":[{\"text\":\"{it}abide by{/it} the rules\"}]}}],[{\"definingText\":{\"text\":[\"{bc}to accept without objection {bc}to {d_link|acquiesce|acquiesce} in \"],\"examples\":[{\"text\":\"will {it}abide by{/it} your decision\"}]}}]]}]}"

    it("can be read from JSON") {
      val definition = Json
        .parse(webster)
        .validate[WebsterDefinedRunOnPhrase]
        .get
      assert(definition.definedRunOnPhrase == "abide by")

      assert(definition.definition.head.senseSequence.isDefined)
      val sseq = definition.definition.head.senseSequence.get
      assert(sseq.size == 2)
    }

    it("can be written back out to JSON") {
      val input = Json
        .parse(webster)
        .validate[WebsterDefinedRunOnPhrase]
        .get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
