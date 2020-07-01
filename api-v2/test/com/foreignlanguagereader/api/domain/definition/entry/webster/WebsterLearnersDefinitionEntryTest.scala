package com.foreignlanguagereader.api.domain.definition.entry.webster

import com.foreignlanguagereader.api.util.ContentFileLoader
import org.scalatest.funspec.AnyFunSpec

class WebsterLearnersDefinitionEntryTest extends AnyFunSpec {
  describe("A learners definition entry") {
    it("can be read from the entry for example") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterLearnersDefinitionEntry]](
          "/resources/webster/learners/learnersDictionaryExample.json"
        )(WebsterLearnersDefinitionEntry.helper.readsSeq)
      println(webster)
    }

    it("can be read from the entry for pop") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterLearnersDefinitionEntry]](
          "/resources/webster/learners/learnersDictionaryPop.json"
        )(WebsterLearnersDefinitionEntry.helper.readsSeq)
      println(webster)
    }

    it("can be read from the entry for test") {
      val webster = ContentFileLoader
        .loadJsonResourceFile[Seq[WebsterLearnersDefinitionEntry]](
          "/resources/webster/learners/learnersDictionaryTest.json"
        )(WebsterLearnersDefinitionEntry.helper.readsSeq)
      println(webster)
    }
  }
}
