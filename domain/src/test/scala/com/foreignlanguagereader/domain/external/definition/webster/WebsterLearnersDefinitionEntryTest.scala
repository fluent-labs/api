package com.foreignlanguagereader.domain.external.definition.webster

import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.util.ContentFileLoader
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
        "{bc}a person or way of behaving that is seen as a model that should be followed ",
        "{bc}someone or something that is mentioned to help explain what you are saying or to show that a general statement is true ",
        "{bc}something or someone chosen from a group in order to show what the whole group is like ",
        "{bc}a phrase or sentence that shows how a word is used ",
        "{bc}something (such as a problem that a student has to solve) that is used to teach how a rule or process works "
      )
      val examples = Some(
        List(
          "He was inspired by the {it}example{/it} of his older brother. [=he wanted to do what his older brother did]",
          "You should try to follow her {it}example{/it}. [=try to do as she does]",
          "Let that be an {it}example{/it} to you! [=let that show you what you should or should not do]",
          "He set a good/bad {it}example{/it} for the rest of us.",
          "It's up to you to {phrase}set an example{/phrase}. [=to behave in a way that shows other people how to behave]",
          "She chooses to {phrase}lead by example{/phrase}. [=to lead by behaving in a way that shows others how to behave]",
          "She gave/offered several {it}examples{/it} to show that the program is effective.",
          "We've chosen three {it}examples{/it} of contemporary architecture for closer study.",
          "a classic {it}example{/it} of a Persian rug",
          "a fine/prime {it}example{/it} of the artist's work",
          "The dictionary includes thousands of {it}examples{/it}.",
          "arithmetic {it}examples{/it}"
        )
      )

      it("can be read from the webster payload") {
        assert(webster.size == 6)

        val example = webster(0)
        assert(example.token == token)
        assert(example.subdefinitions == subdefinitions)
        assert(example.tag.contains(tag))
        assert(example.examples == examples)
      }

      it("can convert to a Definition") {
        val wordLanguage = Language.ENGLISH
        val definitionLanguage = Language.ENGLISH
        val source = DefinitionSource.MIRRIAM_WEBSTER_LEARNERS

        val compareAgainst = Definition(
          subdefinitions,
          ipa,
          tag,
          examples,
          wordLanguage,
          definitionLanguage,
          source,
          token
        )

        assert(webster(0).toDefinition(PartOfSpeech.NOUN) == compareAgainst)
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

        val pop = webster(0)
        assert(pop.token == "pop")
        assert(
          pop.subdefinitions == List(
            "{bc}to suddenly break open or come away from something often with a short, loud noise ",
            "{bc}to make a short, loud noise ",
            "{bc}to cook (popcorn) ",
            "{bc}to come from, into, or out of a place suddenly or briefly ",
            "{bc}to go to or from a place quickly, suddenly, or briefly ",
            "{bc}to put (something) in, into, or onto a place suddenly or briefly ",
            "{bc}to hit (someone) ",
            "{bc}to hit a pop fly ",
            "{bc}to open and drink (a bottle or can of beer) "
          )
        )
        assert(pop.tag.contains(PartOfSpeech.VERB))
        assert(
          pop.examples.contains(
            List(
              "The balloon {it}popped{/it}. [={it}burst{/it}]",
              "We heard the sound of corks {it}popping{/it} as the celebration began.",
              "One of the buttons {it}popped{/it} off my sweater.",
              "Don't {it}pop{/it} that balloon!",
              "She {it}popped{/it} the cork on the champagne. [=she opened the bottle of champagne by removing the cork]",
              "Guns were {it}popping{/it} in the distance.",
              "We {it}popped{/it} some popcorn in the microwave.",
              "The popcorn is done {it}popping{/it}.",
              "I didn't mean to say that—it just {it}popped{/it} out.",
              "Her shoulder {it}popped{/it} out of its socket.",
              "He opened the box, and out {it}popped{/it} a mouse.",
              "A funny thought just {it}popped{/it} into my head. [=I just thought of something funny]",
              "The cathedral suddenly {it}popped{/it} into view. [=I could suddenly see the cathedral]",
              "Her father {it}pops{/it} in and out of her life. [=her father is sometimes involved in her life and sometimes not]",
              "If you are busy, I can {it}pop{/it} back in later.",
              "She {it}popped{/it} over for a cup of tea. = ({it}Brit{/it}) She {it}popped{/it} round for a cup of tea.",
              "My neighbor {it}popped{/it} in for a visit.",
              "I need to {it}pop{/it} into the drugstore for some film.",
              "She {it}popped{/it} out for a minute. She should be back soon.",
              "I'll {it}pop{/it} down to the post office during my break.",
              "She {it}popped{/it} a CD in the player.",
              "He {it}popped{/it} a quarter in the jukebox.",
              "I {it}popped{/it} a grape into my mouth.",
              "He {it}popped{/it} [={it}stuck{/it}] his head out the window.",
              "I felt like {it}popping{/it} him (one).",
              "He {it}popped{/it} [={it}popped up{/it}] to the second baseman in the first inning.",
              "The batter {phrase}popped out{/phrase}. [=hit a pop fly that was caught for an out]",
              "They stopped at a bar to {it}pop{/it} a few beers after work."
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

        val test = webster(0)
        assert(test.token == "test")
        assert(
          test.subdefinitions == List(
            "{bc}a set of questions or problems that are designed to measure a person's knowledge, skills, or abilities ",
            "{dx}see also {dxt|intelligence test||} {dxt|rorschach test||} {dxt|screen test||}{/dx}",
            "{bc}a careful study of a part of the body or of a substance taken from the body ",
            "{dx}see also {dxt|blood test||} {dxt|breath test||} {dxt|stress test||}{/dx}",
            "{bc}a careful study of a small amount of water, soil, air, etc., in order to see if its quality is good, to find out if it contains a dangerous substance, etc. ",
            "{bc}a planned and usually controlled act or series of acts that is done to learn something, to see if something works properly, etc. ",
            "{dx}see also {dxt|road test||}{/dx}",
            "{bc}something (such as a difficult situation or task) that shows how strong or skilled someone or something is ",
            "{dx}see also {dxt|acid test||} {dxt|litmus test||}{/dx}",
            "{bc}{sx|test match||}"
          )
        )
        assert(test.tag.contains(PartOfSpeech.NOUN))
        assert(
          test.examples.contains(
            List(
              "She is studying for her math/spelling/history {it}test{/it}.",
              "I passed/failed/flunked my biology {it}test{/it}.",
              "The teacher sat at his desk grading {it}tests{/it}.",
              "a driver's/driving {it}test{/it} [=a test that is used to see if someone is able to safely drive a car]",
              "an IQ {it}test{/it}",
              "{it}test{/it} questions",
              "The {it}test{/it} will be on [=the questions on the test will be about] the first three chapters of the book.",
              "We {phrase}took/had a test{/phrase} on European capitals. = ({it}Brit{/it}) We {phrase}did a test{/phrase} on European capitals.",
              "The college relies on {phrase}test scores{/phrase} in its admissions process.",
              "The {it}test{/it} showed/revealed a problem with your liver function.",
              "a vision/hearing {it}test{/it} [=a test that shows how well you see/hear]",
              "a urine {it}test{/it} [=a test that examines a person's urine for evidence of disease or illegal drugs]",
              "allergy {it}tests{/it} [=tests that show what you are allergic to]",
              "All applicants must pass a {phrase}drug test{/phrase}. [=a test that examines a person's blood or urine for evidence of illegal drugs]",
              "The doctor will call you with the {phrase}test results{/phrase}.",
              "They went to the drug store to buy a {phrase}pregnancy test{/phrase}. [=a device that reacts to a woman's urine in a way that shows whether or not she is pregnant]",
              "a {phrase}DNA test{/phrase} [=a test that examines DNA and that is used to identify someone or to show that people are relatives]",
              "The {it}test{/it} indicated high levels of lead in the soil.",
              "routine water {it}tests{/it}",
              "lab/laboratory {it}tests{/it}",
              "underground nuclear {it}tests{/it}",
              "a {it}test{/it} of a new vaccine",
              "{phrase}Taste tests{/phrase} revealed that people prefer this brand of cola over that one.",
              "a {it}test{/it} of will/strength/character",
              "The real/true {it}test{/it} of your ability as a skier is whether you can ski well on very hard snow."
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
