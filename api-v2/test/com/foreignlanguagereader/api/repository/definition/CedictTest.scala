package com.foreignlanguagereader.api.repository.definition

import org.scalatest.funspec.AnyFunSpec

class CedictTest extends AnyFunSpec {
  it("can parse CEDICT") {
    assert(Cedict.definitions.nonEmpty)
  }

  it("can get definitions") {
    val library = Cedict.getDefinition("圖書館")
    assert(library.isDefined)
    val libraryDefinition = library.get(0)
    assert(libraryDefinition.pinyin == "tu2 shu1 guan3")
    assert(libraryDefinition.traditional == "圖書館")
    assert(libraryDefinition.simplified == "图书馆")
    assert(libraryDefinition.subdefinitions.size == 2)
    assert(libraryDefinition.subdefinitions(0) == "library")
    assert(libraryDefinition.subdefinitions(1) == "CL:家[jia1],個|个[ge4]")
  }
}
