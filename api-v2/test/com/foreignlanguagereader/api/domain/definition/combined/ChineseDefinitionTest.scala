package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.ChineseDefinitionDTO

class ChineseDefinitionTest extends org.scalatest.FunSpec {
  describe("A single character Chinese definition") {
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
    describe("when getting pronunciation") {
      it("can determine pronunciation from pinyin") {
        assert(example.ipa == "[xɑʊ̯]")
        assert(example.zhuyin == "ㄏㄠ")
        assert(example.wadeGiles == "hao3")
      }
      it("does not break if invalid pinyin are provided") {
        val badPinyin = example.copy(pinyin = "invalid")
        assert(badPinyin.ipa == "")
        assert(badPinyin.zhuyin == "")
        assert(badPinyin.wadeGiles == "")
      }
      it("does not break if no pinyin are provided") {
        val noPinyin = example.copy(pinyin = "")
      }
    }
    it("can get HSK level") {
      assert(example.hsk == HSKLevel.ONE)
    }
    it("can convert itself to a DTO") {
      val compareAgainst = ChineseDefinitionDTO(
        example.subdefinitions,
        example.tag,
        example.examples,
        example.pinyin,
        example.simplified,
        example.traditional,
        example.ipa,
        example.zhuyin,
        example.wadeGiles,
        example.hsk
      )
      assert(example.toDTO == compareAgainst)
    }
  }

  describe("A multi character chinese definition") {
    val example = ChineseDefinition(
      List("definition 1", "definition 2"),
      "noun",
      List("example 1", "example 2"),
      "ni3 hao3",
      "你好",
      "你好",
      DefinitionSource.MULTIPLE,
      token = "你好"
    )

    describe("when getting pronunciation") {
      it("can determine pronunciation from pinyin") {
        assert(example.ipa == "[ni] [xɑʊ̯]")
        assert(example.zhuyin == "ㄋㄧ ㄏㄠ")
        assert(example.wadeGiles == "ni3 hao3")
      }
      it("does not break if invalid pinyin are provided") {
        val badPinyin = example.copy(pinyin = "invalid")
        assert(badPinyin.ipa == "")
        assert(badPinyin.zhuyin == "")
        assert(badPinyin.wadeGiles == "")
      }
    }
    it("does not break if there is no HSK level") {
      assert(example.hsk == HSKLevel.NONE)
    }
  }
}
