package com.foreignlanguagereader.domain.external.definition.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterHeadwordInfoTest extends AnyFunSpec {
  describe("a headword info section") {
    val webster =
      "{\"hw\":\"pa*ja*ma\",\"prs\":[{\"mw\":\"p\\u0259-\\u02c8j\\u00e4-m\\u0259\",\"sound\":{\"audio\":\"pajama02\",\"ref\":\"c\",\"stat\":\"1\"}},{\"mw\":\"-\\u02c8ja-\",\"sound\":{\"audio\":\"pajama01\",\"ref\":\"c\",\"stat\":\"1\"}}]}"
    val domain =
      "{\"headword\":\"pa*ja*ma\",\"pronunciations\":[{\"writtenPronunciation\":\"pə-ˈjä-mə\",\"sound\":{\"audio\":\"pajama02\",\"language\":\"en\",\"country\":\"us\"}},{\"writtenPronunciation\":\"-ˈja-\",\"sound\":{\"audio\":\"pajama01\",\"language\":\"en\",\"country\":\"us\"}}]}"

    it("can be read from JSON") {
      val headword = Json.parse(webster).validate[WebsterHeadwordInfo].get
      assert(headword.headword == "pa*ja*ma")
      assert(
        headword.pronunciations.get.head.sound.get.audioUrl == "https://media.merriam-webster.com/audio/prons/en/us/mp3/p/pajama02.mp3"
      )
    }

    it("can be written back out to JSON") {
      val input = Json.parse(webster).validate[WebsterHeadwordInfo].get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
