package com.foreignlanguagereader.api.domain.definition.entry.webster

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.domain.word.PartOfSpeech
import com.foreignlanguagereader.api.util.ContentFileLoader
import org.scalatest.funspec.AnyFunSpec
import play.api.libs.json.{JsValue, Json}

class WebsterSpanishDefinitionEntryTest extends AnyFunSpec {
  describe("A spanish definition entry") {
    describe(
      "for a word that is the same in both english and spanish: 'animal'"
    ) {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterAnimal.json"
        )(WebsterSpanishDefinitionEntry.helper.readsSeq)
      val output = ContentFileLoader
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainAnimal.json")
        .toString()

      val tag = Some(PartOfSpeech.NOUN)
      val token = "animal"
      val subdefinitions =
        List("{bc}animal ", "{sx|brute||} {bc}{a_link|bruto} ", ", ", "  ")
      val examples = List()

      it("can be read from the webster payload") {
        assert(webster.size == 4)

        val example = webster(0)
        assert(example.token == token)
        assert(example.subdefinitions == subdefinitions)
        assert(example.tag == tag)
        assert(example.examples == examples)
      }

      it("can convert to a Definition") {
        val wordLanguage = Language.ENGLISH
        val definitionLanguage = Language.SPANISH
        val source = DefinitionSource.MIRRIAM_WEBSTER_SPANISH

        val compareAgainst = Definition(
          subdefinitions,
          tag,
          examples,
          wordLanguage,
          definitionLanguage,
          source,
          token
        )

        assert(webster(0).toDefinition == compareAgainst)
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'pero'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPero.json"
        )(WebsterSpanishDefinitionEntry.helper.readsSeq)
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
        assert(pop.tag == "masculine noun")
        assert(pop.examples == List("ponerle peros a"))
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'perro'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPerro.json"
        )(WebsterSpanishDefinitionEntry.helper.readsSeq)
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
        assert(perro.tag == "noun")
        assert(perro.examples == List())
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'pop'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPop.json"
        )(WebsterSpanishDefinitionEntry.helper.readsSeq)
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
        assert(pop.tag == "verb")
        assert(
          pop.examples == List(
            "he popped into the house",
            "a menu pops up",
            "my eyes popped out of my head",
            "he popped it into his mouth",
            "she popped her head out the window"
          )
        )
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'porque'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterPorque.json"
        )(WebsterSpanishDefinitionEntry.helper.readsSeq)
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
        assert(perro.tag == "masculine noun")
        assert(perro.examples == List("no explicó el porqué"))
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }

    describe("for 'vale'") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterSpanishDefinitionEntry]](
          "/webster/spanish/websterVale.json"
        )(WebsterSpanishDefinitionEntry.helper.readsSeq)
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
        assert(perro.tag == "masculine noun")
        assert(perro.examples == List())
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }
  }
}