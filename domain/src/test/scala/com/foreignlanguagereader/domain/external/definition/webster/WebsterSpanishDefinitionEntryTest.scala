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

class WebsterSpanishDefinitionEntryTest extends AnyFunSpec {
  describe("A spanish definition entry") {
    describe(
      "for a word that is the same in both english and spanish: 'animal'"
    ) {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterAnimal.json"
        )(WebsterSpanishDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainAnimal.json")
        .toString()

      val tag = PartOfSpeech.NOUN
      val ipa = "ˈænəm{it}ə{/it}l"
      val token = "animal"
      val subdefinitions =
        List("{bc}animal ", "{sx|brute||} {bc}{a_link|bruto} ", ", ", "  ")
      val examples = None

      it("can be read from the webster payload") {
        assert(webster.size == 4)

        val example = webster(0)
        assert(example.token == token)
        assert(example.subdefinitions == subdefinitions)
        assert(example.tag.contains(tag))
        assert(example.examples == examples)
      }

      it("can convert to a Definition") {
        val wordLanguage = Language.ENGLISH
        val definitionLanguage = Language.SPANISH
        val source = DefinitionSource.MIRRIAM_WEBSTER_SPANISH

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

    describe("for 'pero'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPero.json"
        )(WebsterSpanishDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPero.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 2)

        val pop = webster(0)
        assert(pop.token == "pero")
        assert(
          pop.subdefinitions == List(
            "{bc}{a_link|fault}, {a_link|defect} ",
            "{bc}{a_link|objection}"
          )
        )
        assert(pop.tag.contains(PartOfSpeech.NOUN))
        assert(pop.examples.contains(List("ponerle peros a")))
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'perro'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPerro.json"
        )(WebsterSpanishDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPerro.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 3)

        val perro = webster(0)
        assert(perro.token == "perro")
        assert(
          perro.subdefinitions == List(
            "{bc}{a_link|dog}, {a_link|bitch} ",
            "{bc}stray dog",
            "{bc}{a_link|hot dog}",
            "{bc}{a_link|retriever}",
            "{bc}{a_link|lapdog}",
            "{bc}{a_link|guard dog}",
            "{bc}{a_link|guide dog}",
            "{bc}{a_link|sheepdog}",
            "{bc}police dog",
            "{bc}tracking dog",
            "{bc}{a_link|dachshund}"
          )
        )
        assert(perro.tag.contains(PartOfSpeech.NOUN))
        assert(perro.examples.isEmpty)
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'pop'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPop.json"
        )(WebsterSpanishDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPop.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 6)

        val pop = webster(0)
        assert(pop.token == "pop")
        assert(
          pop.subdefinitions == List(
            "{sx|burst||} {bc}{a_link|reventarse}, {a_link|estallar}",
            "{bc}{a_link|saltar} (dícese de un corcho)",
            "{bc}{a_link|ir}, {a_link|venir}, o aparecer abruptamente ",
            "{sx|protrude||} {bc}{a_link|salirse}, {a_link|saltarse} ",
            "{bc}proponerle matrimonio a alguien",
            "{sx|burst||} {bc}{a_link|reventar}",
            "{bc}sacar o meter abruptamente "
          )
        )
        assert(pop.tag.contains(PartOfSpeech.VERB))
        assert(
          pop.examples.contains(
            List(
              "he popped into the house",
              "a menu pops up",
              "my eyes popped out of my head",
              "he popped it into his mouth",
              "she popped her head out the window"
            )
          )
        )
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'porque'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPorque.json"
        )(WebsterSpanishDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPorque.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 6)

        val perro = webster(1)
        assert(perro.token == "porque'")
        assert(
          perro.subdefinitions == List("{bc}{a_link|reason}, {a_link|cause} ")
        )
        assert(perro.tag.contains(PartOfSpeech.NOUN))
        assert(perro.examples.contains(List("no explicó el porqué")))
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'vale'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[List[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterVale.json"
        )(WebsterSpanishDefinitionEntry.helper.readsList)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainVale.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 4)

        val perro = webster(1)
        assert(perro.token == "vale")
        assert(
          perro.subdefinitions == List(
            "{bc}{a_link|voucher}",
            "{sx|pagaré||} {bc}promissory note, {a_link|IOU}"
          )
        )
        assert(perro.tag.contains(PartOfSpeech.NOUN))
        assert(perro.examples.isEmpty)
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }
  }
}
