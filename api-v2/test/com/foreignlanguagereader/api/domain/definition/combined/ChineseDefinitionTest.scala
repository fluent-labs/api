package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.ChineseDefinitionDTO
import org.scalatest.funspec.AnyFunSpec

class ChineseDefinitionTest extends AnyFunSpec {
  describe("A single character Chinese definition") {
    val example = ChineseDefinition(
      List("definition 1", "definition 2"),
      "noun",
      List("example 1", "example 2"),
      "hao3",
      "好",
      "好",
      Language.ENGLISH,
      DefinitionSource.MULTIPLE,
      token = "好"
    )

    describe("when getting pronunciation") {
      it("can determine pronunciation from pinyin") {
        assert(example.pinyin == "hao")
        assert(example.ipa == "[xɑʊ̯]")
        assert(example.zhuyin == "ㄏㄠ")
        assert(example.wadeGiles == "hao")
      }
      it("does not break if invalid pinyin are provided") {
        val badPinyin = example.copy(inputPinyin = "invalid")
        assert(badPinyin.pinyin == "")
        assert(badPinyin.ipa == "")
        assert(badPinyin.zhuyin == "")
        assert(badPinyin.wadeGiles == "")
      }
      it("does not break if no pinyin are provided") {
        val noPinyin = example.copy(inputPinyin = "")
        assert(noPinyin.pinyin == "")
        assert(noPinyin.ipa == "")
        assert(noPinyin.zhuyin == "")
        assert(noPinyin.wadeGiles == "")
      }
    }
    it("can get HSK level") {
      assert(example.hsk == HskLevel.ONE)
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
      Language.ENGLISH,
      DefinitionSource.MULTIPLE,
      token = "你好"
    )

    describe("when getting pronunciation") {
      it("can determine pronunciation from pinyin") {
        assert(example.ipa == "[ni] [xɑʊ̯]")
        assert(example.zhuyin == "ㄋㄧ ㄏㄠ")
        assert(example.wadeGiles == "ni hao")
      }
      it("does not break if invalid pinyin are provided") {
        val badPinyin = example.copy(inputPinyin = "invalid")
        assert(badPinyin.pinyin == "")
        assert(badPinyin.ipa == "")
        assert(badPinyin.zhuyin == "")
        assert(badPinyin.wadeGiles == "")
      }
    }
    it("does not break if there is no HSK level") {
      assert(example.hsk == HskLevel.NONE)
    }
  }
}
