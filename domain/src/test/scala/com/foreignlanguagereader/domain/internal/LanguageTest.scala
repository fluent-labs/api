package com.foreignlanguagereader.domain.internal

import com.foreignlanguagereader.domain.Language

class LanguageTest extends org.scalatest.funsuite.AnyFunSuite {

  test("Enums should evaluate to their value using toString") {
    assert(Language.CHINESE.toString == "CHINESE")
    assert(Language.ENGLISH.toString == "ENGLISH")
    assert(Language.SPANISH.toString == "SPANISH")
  }

  test("Enums can be read from a string") {
    assert(Language.fromString("CHINESE").isDefined)
    assert(Language.fromString("CHINESE").get == Language.CHINESE)
    assert(Language.fromString("KLINGON").isEmpty)
  }
}
