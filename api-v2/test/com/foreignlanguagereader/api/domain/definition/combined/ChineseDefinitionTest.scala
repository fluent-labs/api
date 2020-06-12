package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource

class ChineseDefinitionTest extends org.scalatest.FunSuite {
  val example = ChineseDefinition(
    List("definition 1", "definition 2"),
    "noun",
    List("example 1", "example 2"),
    "hao3",
    "好",
    "好",
    DefinitionSource.MULTIPLE,
    token = "好"
  )

  test("definitions can get pronunciation data") {
    assert(example.ipa == "[xɑʊ̯]")
    assert(example.zhuyin == "ㄏㄠ")
    assert(example.wadeGiles == "hao")
  }
}
