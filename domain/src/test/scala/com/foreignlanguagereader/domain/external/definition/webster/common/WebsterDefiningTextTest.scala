package com.foreignlanguagereader.domain.external.definition.webster.common

import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.Json

class WebsterDefiningTextTest extends AnyFunSpec {
  describe("a defining text") {
    describe("with a text field") {
      val webster =
        "[[\"text\",\"{bc}a person or way of behaving that is seen as a model that should be followed \"],[\"wsgram\",\"count\"],[\"vis\",[{\"t\":\"He was inspired by the {it}example{/it} of his older brother. [=he wanted to do what his older brother did]\"},{\"t\":\"You should try to follow her {it}example{/it}. [=try to do as she does]\"},{\"t\":\"Let that be an {it}example{/it} to you! [=let that show you what you should or should not do]\"},{\"t\":\"He set a good/bad {it}example{/it} for the rest of us.\"},{\"t\":\"It's up to you to {phrase}set an example{/phrase}. [=to behave in a way that shows other people how to behave]\"}]],[\"wsgram\",\"noncount\"],[\"vis\",[{\"t\":\"She chooses to {phrase}lead by example{/phrase}. [=to lead by behaving in a way that shows others how to behave]\"}]]]"
      val domain =
        "{\"text\":[\"{bc}a person or way of behaving that is seen as a model that should be followed \"],\"examples\":[{\"text\":\"He was inspired by the {it}example{/it} of his older brother. [=he wanted to do what his older brother did]\"},{\"text\":\"You should try to follow her {it}example{/it}. [=try to do as she does]\"},{\"text\":\"Let that be an {it}example{/it} to you! [=let that show you what you should or should not do]\"},{\"text\":\"He set a good/bad {it}example{/it} for the rest of us.\"},{\"text\":\"It's up to you to {phrase}set an example{/phrase}. [=to behave in a way that shows other people how to behave]\"},{\"text\":\"She chooses to {phrase}lead by example{/phrase}. [=to lead by behaving in a way that shows others how to behave]\"}]}"
      it("can be read from JSON") {
        val definingText =
          Json.parse(webster).validate[WebsterDefiningText].get
        assert(definingText.text.size == 1)
        assert(
          definingText.text.head == "{bc}a person or way of behaving that is seen as a model that should be followed "
        )
      }

      it("can be written back out to JSON") {
        val input =
          Json.parse(webster).validate[WebsterDefiningText].get
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
        val definingText =
          Json.parse(webster).validate[WebsterDefiningText].get
        assert(definingText.biographicalName.isDefined)
        val biographicalNameWrap = definingText.biographicalName.get
        assert(biographicalNameWrap.size == 2)
        assert(
          biographicalNameWrap.head.personalName.get == "Charles Lut*widge"
        )
        assert(biographicalNameWrap(1).alternateName.get == "Lewis Car*roll")
      }

      it("can be written back out to JSON") {
        val input =
          Json.parse(webster).validate[WebsterDefiningText].get
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
        val definingText =
          Json.parse(webster).validate[WebsterDefiningText].get

        assert(definingText.calledAlso.isDefined)
        val calledAlso = definingText.calledAlso.get
        assert(calledAlso.size == 1)

        assert(calledAlso.head.calledAlsoTargets.isDefined)
        val calledAlsoTargets = calledAlso.head.calledAlsoTargets.get
        assert(calledAlsoTargets(1).areaOfUsage.isDefined)
        assert(calledAlsoTargets(1).areaOfUsage.get == "({it}chiefly US{/it}) ")
      }

      it("can be written back out to JSON") {
        val input =
          Json.parse(webster).validate[WebsterDefiningText].get
        val output = Json.toJson(input).toString()
        assert(output == domain)
      }
    }

    describe("with a supplemental note") {
      val webster =
        "[[\"text\",\"{bc}any of a genus ({it}Trichechus{\\/it} of the family Trichechidae) of large, herbivorous, aquatic mammals that inhabit warm coastal and inland waters of the southeastern U.S., West Indies, northern South America, and West Africa and have a rounded body, a small head with a squarish snout, paddle-shaped flippers usually with vestigial nails, and a flattened, rounded tail used for propulsion \"],[\"snote\",[[\"t\",\"Manatees are {d_link|sirenians|sirenian} related to and resembling the {d_link|dugong|dugong} but differing most notably in the shape of the tail.\"],[\"vis\",[{\"t\":\"An aquatic relative of the elephant, {wi}manatees{\\/wi} grow up to nine feet long and can weigh 1,000 pounds.\",\"aq\":{\"auth\":\"Felicity Barringer\"}}]]]]]"
      val domain =
        "{\"text\":[\"{bc}any of a genus ({it}Trichechus{/it} of the family Trichechidae) of large, herbivorous, aquatic mammals that inhabit warm coastal and inland waters of the southeastern U.S., West Indies, northern South America, and West Africa and have a rounded body, a small head with a squarish snout, paddle-shaped flippers usually with vestigial nails, and a flattened, rounded tail used for propulsion \"],\"supplementalNote\":[{\"text\":\"Manatees are {d_link|sirenians|sirenian} related to and resembling the {d_link|dugong|dugong} but differing most notably in the shape of the tail.\",\"example\":[{\"text\":\"An aquatic relative of the elephant, {wi}manatees{/wi} grow up to nine feet long and can weigh 1,000 pounds.\"}]}]}"

      it("can be read from JSON") {
        val definingText =
          Json.parse(webster).validate[WebsterDefiningText].get

        assert(definingText.supplementalNote.isDefined)
        val supplementalNotes = definingText.supplementalNote.get
        assert(supplementalNotes.size == 1)

        val supplementalNote = supplementalNotes.head
        assert(
          supplementalNote.text == "Manatees are {d_link|sirenians|sirenian} related to and resembling the {d_link|dugong|dugong} but differing most notably in the shape of the tail."
        )
      }

      it("can be written back out to JSON") {
        val input =
          Json.parse(webster).validate[WebsterDefiningText].get
        val output = Json.toJson(input).toString()
        assert(output == domain)
      }
    }
  }
}
