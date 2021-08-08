package com.foreignlanguagereader.jobs.definitions.source

import io.fluentlabs.content.types.external.definition.cedict.CEDICTDefinitionEntry
import org.scalatest.funspec.AnyFunSpec

class CEDICTTest extends AnyFunSpec {
  describe("Can parse a definition line") {
    it("for the happy path") {
      val result = CEDICT.parseLine(
        "2019冠狀病毒病 2019冠状病毒病 [er4 ling2 yi1 jiu3 guan1 zhuang4 bing4 du2 bing4] /COVID-19, the coronavirus disease identified in 2019/"
      )
      assert(
        result == CEDICTDefinitionEntry(
          List("COVID-19, the coronavirus disease identified in 2019"),
          "er4 ling2 yi1 jiu3 guan1 zhuang4 bing4 du2 bing4",
          "2019冠状病毒病",
          "2019冠狀病毒病",
          "2019冠狀病毒病"
        )
      )
    }

    it("with different simplified and traditional characters") {
      val result =
        CEDICT.parseLine("502膠 502胶 [wu3 ling2 er4 jiao1] /cyanoacrylate glue/")
      assert(
        result == CEDICTDefinitionEntry(
          List("cyanoacrylate glue"),
          "wu3 ling2 er4 jiao1",
          "502胶",
          "502膠",
          "502膠"
        )
      )
    }

    it("with multiple subdefinitions") {
      val result =
        CEDICT.parseLine("AA制 AA制 [A A zhi4] /to split the bill/to go Dutch/")
      assert(
        result == CEDICTDefinitionEntry(
          List("to split the bill", "to go Dutch"),
          "A A zhi4",
          "AA制",
          "AA制",
          "AA制"
        )
      )
    }
  }
}
