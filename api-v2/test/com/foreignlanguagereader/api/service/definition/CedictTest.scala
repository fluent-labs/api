package com.foreignlanguagereader.api.service.definition

import org.scalatest.funspec.AnyFunSpec

class CedictTest extends AnyFunSpec {
  it("can parse CEDICT") {
    val cedict = Cedict.definitions
  }

  describe("can convert characters to traditional characters") {
    it("does nothing when they are already traditional") {
      assert(Cedict.convertToTraditional("圖書館") == "圖書館")
    }

    it("can convert from simplified") {
      assert(Cedict.convertToTraditional("图书馆") == "圖書館")
    }

    it("does nothing if the word isn't found") {
      assert(Cedict.convertToTraditional("ABCD") == "ABCD")
    }
  }
}
