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

  it("can get definitions") {
    val library = Cedict.getDefinition("圖書館")
    assert(library.isDefined)
    val libraryDefinition = library.get
    assert(libraryDefinition.pinyin == "tu2 shu1 guan3")
    assert(libraryDefinition.traditional == "圖書館")
    assert(libraryDefinition.simplified == "图书馆")
    assert(libraryDefinition.subdefinitions.size == 2)
    assert(libraryDefinition.subdefinitions(0) == "library")
    assert(libraryDefinition.subdefinitions(1) == "CL:家[jia1],個|个[ge4]")
  }
}
