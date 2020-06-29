package com.foreignlanguagereader.api.domain.definition.entry.webster

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.{JsValue, Json, Reads}

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
      assert(output == domain)
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

  describe("a definition section") {
    implicit val readsSeq: Reads[Seq[JsValue]] = Reads.seq[JsValue]
    implicit val readsSeqSeq: Reads[Seq[Seq[JsValue]]] = Reads.seq(readsSeq)

    describe("a defining text") {
      describe("with a text field") {
        val webster =
          "[[\"text\",\"{bc}a person or way of behaving that is seen as a model that should be followed \"],[\"wsgram\",\"count\"],[\"vis\",[{\"t\":\"He was inspired by the {it}example{/it} of his older brother. [=he wanted to do what his older brother did]\"},{\"t\":\"You should try to follow her {it}example{/it}. [=try to do as she does]\"},{\"t\":\"Let that be an {it}example{/it} to you! [=let that show you what you should or should not do]\"},{\"t\":\"He set a good/bad {it}example{/it} for the rest of us.\"},{\"t\":\"It's up to you to {phrase}set an example{/phrase}. [=to behave in a way that shows other people how to behave]\"}]],[\"wsgram\",\"noncount\"],[\"vis\",[{\"t\":\"She chooses to {phrase}lead by example{/phrase}. [=to lead by behaving in a way that shows others how to behave]\"}]]]"
        val domain =
          "{\"text\":[\"{bc}a person or way of behaving that is seen as a model that should be followed \"]}"

        it("can be read from JSON") {
          val definingTextRaw =
            Json.parse(webster).validate[Seq[Seq[JsValue]]].get
          val definingText =
            WebsterDefiningText.parseDefiningText(definingTextRaw)
          assert(definingText.text.size == 1)
          assert(
            definingText.text(0) == "{bc}a person or way of behaving that is seen as a model that should be followed "
          )
        }

        it("can be written back out to JSON") {
          val definingTextRaw =
            Json.parse(webster).validate[Seq[Seq[JsValue]]].get
          val input =
            WebsterDefiningText.parseDefiningText(definingTextRaw)
          val output = Json.toJson(input).toString()
          assert(output == domain)
        }
      }

      describe("with a biological name wrap") {
        val webster =
          "[[\"bnw\",{\"pname\":\"Charles Lut*widge\",\"prs\":[{\"mw\":\"ˈlət-wij\",\"sound\":{\"audio\":\"bixdod04\",\"ref\":\"c\",\"stat\":\"1\"}}]}],[\"text\",\"1832–1898 pseudonym\"],[\"bnw\",{\"altname\":\"Lewis Car*roll\",\"prs\":[{\"mw\":\"ˈker-əl\",\"sound\":{\"audio\":\"bixdod05\",\"ref\":\"c\",\"stat\":\"1\"}},{\"mw\":\"ˈka-rəl\"}]}],[\"text\",\" English mathematician and writer\"]]"
        val domain =
          "{\"text\":[\"1832–1898 pseudonym\",\" English mathematician and writer\"],\"biographicalName\":[{\"personalName\":\"Charles Lut*widge\",\"pronunciations\":[{\"writtenPronunciation\":\"ˈlət-wij\",\"sound\":{\"audio\":\"bixdod04\",\"language\":\"en\",\"country\":\"us\"}}]},{\"alternateName\":\"Lewis Car*roll\",\"pronunciations\":[{\"writtenPronunciation\":\"ˈker-əl\",\"sound\":{\"audio\":\"bixdod05\",\"language\":\"en\",\"country\":\"us\"}},{\"writtenPronunciation\":\"ˈka-rəl\"}]}]}"

        it("can be read from JSON") {
          val definingTextRaw =
            Json.parse(webster).validate[Seq[Seq[JsValue]]].get
          val definingText =
            WebsterDefiningText.parseDefiningText(definingTextRaw)
          assert(definingText.biographicalName.isDefined)
          val biographicalNameWrap = definingText.biographicalName.get
          assert(biographicalNameWrap.size == 2)
          assert(
            biographicalNameWrap(0).personalName.get == "Charles Lut*widge"
          )
          assert(biographicalNameWrap(1).alternateName.get == "Lewis Car*roll")
        }

        it("can be written back out to JSON") {
          val definingTextRaw =
            Json.parse(webster).validate[Seq[Seq[JsValue]]].get
          val input =
            WebsterDefiningText.parseDefiningText(definingTextRaw)
          val output = Json.toJson(input).toString()
          assert(output == domain)
        }
      }

      describe("with a called also") {
        val webster =
          "[[\"text\",\"{bc}a drink consisting of soda water, flavoring, and a sweet syrup \"],[\"ca\",{\"intro\":\"called also\",\"cats\":[{\"cat\":\"pop\"},{\"psl\":\"({it}chiefly US{/it}) \",\"cat\":\"soda\"}]}]]"
        val domain =
          "{\"text\":[\"{bc}a drink consisting of soda water, flavoring, and a sweet syrup \"],\"calledAlso\":[{\"intro\":\"called also\",\"calledAlsoTargets\":[{\"calledAlsoTargetText\":\"pop\"},{\"calledAlsoTargetText\":\"soda\",\"areaOfUsage\":\"({it}chiefly US{/it}) \"}]}]}"

        it("can be read from JSON") {
          val definingTextRaw =
            Json.parse(webster).validate[Seq[Seq[JsValue]]].get
          val definingText =
            WebsterDefiningText.parseDefiningText(definingTextRaw)
        }

        it("can be written back out to JSON") {
          val definingTextRaw =
            Json.parse(webster).validate[Seq[Seq[JsValue]]].get
          val input =
            WebsterDefiningText.parseDefiningText(definingTextRaw)
          val output = Json.toJson(input).toString()
          assert(output == domain)
        }
      }
    }
  }
}
