package com.foreignlanguagereader.api.domain.definition.entry.webster

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
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

      val tag = "noun"
      val token = "animal"
      val subdefinitions = List("animal", "brute : bruto, bruta")
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
        assert(pop.subdefinitions == pop.shortDefinitions.toList)
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

        val perro = webster(1)
        assert(perro.token == "perro")
        assert(perro.subdefinitions == perro.shortDefinitions.toList)
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
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPero.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 6)

        val pop = webster(0)
        assert(pop.token == "pop")
        assert(pop.subdefinitions == pop.shortDefinitions.toList)
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
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPero.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 6)

        val perro = webster(1)
        assert(perro.token == "porque'")
        assert(perro.subdefinitions == perro.shortDefinitions.toList)
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
        .loadJsonResourceFile[JsValue]("/webster/spanish/domainPerro.json")
        .toString()

      it("can be read from the webster payload") {
        assert(webster.size == 4)

        val perro = webster(1)
        assert(perro.token == "vale")
        assert(perro.subdefinitions == perro.shortDefinitions.toList)
        assert(perro.tag == "masculine noun")
        assert(perro.examples == List())
      }

      it("can be written out to json") {
        assert(Json.toJson(webster).toString() == output)
      }
    }
  }
}
