package com.foreignlanguagereader.api.domain.definition.entry.webster

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.{JsError, Json}

class WebsterCommonDefinitionEntryTest extends AnyFunSpec {
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
      assert(output == output)
    }
  }

  describe("a headword info section") {
    val webster =
      "{\"hw\":\"pa*ja*ma\",\"prs\":[{\"mw\":\"p\\u0259-\\u02c8j\\u00e4-m\\u0259\",\"sound\":{\"audio\":\"pajama02\",\"ref\":\"c\",\"stat\":\"1\"}},{\"mw\":\"-\\u02c8ja-\",\"sound\":{\"audio\":\"pajama01\",\"ref\":\"c\",\"stat\":\"1\"}}]}"
    val domain =
      "{\"headword\":\"pa*ja*ma\",\"pronunciations\":[{\"writtenPronunciation\":\"pə-ˈjä-mə\",\"sound\":{\"audio\":\"pajama02\",\"language\":\"en\",\"country\":\"us\"}},{\"writtenPronunciation\":\"-ˈja-\",\"sound\":{\"audio\":\"pajama01\",\"language\":\"en\",\"country\":\"us\"}}]}"

    it("can be read from JSON") {
      val headword = Json.parse(webster).validate[HeadwordInfo].get
      assert(headword.headword == "pa*ja*ma")
      assert(
        headword.pronunciations
          .get(0)
          .sound
          .get
          .audioUrl == "https://media.merriam-webster.com/audio/prons/en/us/mp3/p/pajama02.mp3"
      )
    }

    it("can be written back out to JSON") {
      val input = Json.parse(webster).validate[HeadwordInfo].get
      val output = Json.toJson(input).toString()
      assert(output == domain)
    }
  }
}
