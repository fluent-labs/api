package com.foreignlanguagereader.content.types.external.definition.webster

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  DefinitionSource,
  EnglishDefinition
}
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.content.util.ContentFileLoader
import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.{JsValue, Json}

class WebsterLearnersDefinitionEntryTest extends AnyFunSpec {
  describe("A learners definition entry") {
    describe("for 'example'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterLearnersDefinitionEntry]](
          "/webster/learners/websterExample.json"
        )(WebsterLearnersDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/learners/domainExample.json")
        .toString()

      val tag = PartOfSpeech.NOUN
      val ipa = "ɪgˈzæmpəl"
      val token = "example"
      val subdefinitions = List(
        "a person or way of behaving that is seen as a model that should be followed",
        "someone or something that is mentioned to help explain what you are saying or to show that a general statement is true",
        "something or someone chosen from a group in order to show what the whole group is like",
        "a phrase or sentence that shows how a word is used",
        "something (such as a problem that a student has to solve) that is used to teach how a rule or process works"
      )
      val examples = Some(
        List(
          "He was inspired by the *example* of his older brother.",
          "You should try to follow her *example*.",
          "Let that be an *example* to you!",
          "He set a good/bad *example* for the rest of us.",
          "It's up to you to {phrase}set an example{/phrase}.",
          "She chooses to {phrase}lead by example{/phrase}.",
          "She gave/offered several *examples* to show that the program is effective.",
          "We've chosen three *examples* of contemporary architecture for closer study.",
          "a classic *example* of a Persian rug",
          "a fine/prime *example* of the artist's work",
          "The dictionary includes thousands of *examples*.",
          "arithmetic *examples*"
        )
      )

      it("can be read from the webster payload") {
        assert(webster.size == 6)

        val example = webster.head
        assert(example.token == token)
        assert(example.subdefinitions == subdefinitions)
        assert(example.tag.contains(tag))
        assert(example.examples == examples)
      }

      it("can convert to a Definition") {
        val wordLanguage = Language.ENGLISH
        val definitionLanguage = Language.ENGLISH
        val source = DefinitionSource.MIRRIAM_WEBSTER_LEARNERS

        val compareAgainst = EnglishDefinition(
          subdefinitions,
          ipa,
          tag,
          examples,
          wordLanguage,
          definitionLanguage,
          source,
          token
        )

        assert(webster.head.toDefinition(PartOfSpeech.NOUN) == compareAgainst)
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'pop") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterLearnersDefinitionEntry]](
          "/webster/learners/websterPop.json"
        )(WebsterLearnersDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/learners/domainPop.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 10)

        val pop = webster.head
        assert(pop.token == "pop")
        assert(
          pop.subdefinitions == List(
            "to suddenly break open or come away from something often with a short, loud noise",
            "to make a short, loud noise",
            "to cook (popcorn)",
            "to come from, into, or out of a place suddenly or briefly",
            "to go to or from a place quickly, suddenly, or briefly",
            "to put (something) in, into, or onto a place suddenly or briefly",
            "to hit (someone)",
            "to hit a pop fly",
            "to open and drink (a bottle or can of beer)"
          )
        )
        assert(pop.tag.contains(PartOfSpeech.VERB))
        assert(
          pop.examples.contains(
            List(
              "The balloon *popped*.",
              "We heard the sound of corks *popping* as the celebration began.",
              "One of the buttons *popped* off my sweater.",
              "Don't *pop* that balloon!",
              "She *popped* the cork on the champagne.",
              "Guns were *popping* in the distance.",
              "We *popped* some popcorn in the microwave.",
              "The popcorn is done *popping*.",
              "I didn't mean to say that—it just *popped* out.",
              "Her shoulder *popped* out of its socket.",
              "He opened the box, and out *popped* a mouse.",
              "A funny thought just *popped* into my head.",
              "The cathedral suddenly *popped* into view.",
              "Her father *pops* in and out of her life.",
              "If you are busy, I can *pop* back in later.",
              "She *popped* over for a cup of tea. = (*Brit*) She *popped* round for a cup of tea.",
              "My neighbor *popped* in for a visit.",
              "I need to *pop* into the drugstore for some film.",
              "She *popped* out for a minute. She should be back soon.",
              "I'll *pop* down to the post office during my break.",
              "She *popped* a CD in the player.",
              "He *popped* a quarter in the jukebox.",
              "I *popped* a grape into my mouth.",
              "He *popped* his head out the window.",
              "I felt like *popping* him (one).",
              "He *popped* to the second baseman in the first inning.",
              "The batter {phrase}popped out{/phrase}.",
              "They stopped at a bar to *pop* a few beers after work."
            )
          )
        )
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'test'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterLearnersDefinitionEntry]](
          "/webster/learners/websterTest.json"
        )(WebsterLearnersDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/learners/domainTest.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 10)

        val test = webster.head
        assert(test.token == "test")
        assert(
          test.subdefinitions == List(
            "a set of questions or problems that are designed to measure a person's knowledge, skills, or abilities",
            "{dx}see also {dxt|intelligence test||} {dxt|rorschach test||} {dxt|screen test||}{/dx}",
            "a careful study of a part of the body or of a substance taken from the body",
            "{dx}see also {dxt|blood test||} {dxt|breath test||} {dxt|stress test||}{/dx}",
            "a careful study of a small amount of water, soil, air, etc., in order to see if its quality is good, to find out if it contains a dangerous substance, etc.",
            "a planned and usually controlled act or series of acts that is done to learn something, to see if something works properly, etc.",
            "{dx}see also {dxt|road test||}{/dx}",
            "something (such as a difficult situation or task) that shows how strong or skilled someone or something is",
            "{dx}see also {dxt|acid test||} {dxt|litmus test||}{/dx}",
            "{sx|test match||}"
          )
        )
        assert(test.tag.contains(PartOfSpeech.NOUN))
        assert(
          test.examples.contains(
            List(
              "She is studying for her math/spelling/history *test*.",
              "I passed/failed/flunked my biology *test*.",
              "The teacher sat at his desk grading *tests*.",
              "a driver's/driving *test*",
              "an IQ *test*",
              "*test* questions",
              "The *test* will be on the first three chapters of the book.",
              "We {phrase}took/had a test{/phrase} on European capitals. = (*Brit*) We {phrase}did a test{/phrase} on European capitals.",
              "The college relies on {phrase}test scores{/phrase} in its admissions process.",
              "The *test* showed/revealed a problem with your liver function.",
              "a vision/hearing *test*",
              "a urine *test*",
              "allergy *tests*",
              "All applicants must pass a {phrase}drug test{/phrase}.",
              "The doctor will call you with the {phrase}test results{/phrase}.",
              "They went to the drug store to buy a {phrase}pregnancy test{/phrase}.",
              "a {phrase}DNA test{/phrase}",
              "The *test* indicated high levels of lead in the soil.",
              "routine water *tests*",
              "lab/laboratory *tests*",
              "underground nuclear *tests*",
              "a *test* of a new vaccine",
              "{phrase}Taste tests{/phrase} revealed that people prefer this brand of cola over that one.",
              "a *test* of will/strength/character",
              "The real/true *test* of your ability as a skier is whether you can ski well on very hard snow."
            )
          )
        )
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }
  }
}
