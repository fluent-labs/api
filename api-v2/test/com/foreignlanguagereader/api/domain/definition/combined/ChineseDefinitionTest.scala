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
        assert(example.pronunciation.pinyin == "hao")
        assert(example.pronunciation.ipa == "[xɑʊ̯]")
        assert(example.pronunciation.zhuyin == "ㄏㄠ")
        assert(example.pronunciation.wadeGiles == "hao")
      }
      it("does not break if invalid pinyin are provided") {
        val badPinyin = example.copy(inputPinyin = "invalid3")
        assert(badPinyin.pronunciation.pinyin == "")
        assert(badPinyin.pronunciation.ipa == "")
        assert(badPinyin.pronunciation.zhuyin == "")
        assert(badPinyin.pronunciation.wadeGiles == "")
        assert(badPinyin.pronunciation.tones.isEmpty)
      }
      it("does not break if no pinyin are provided") {
        val noPinyin = example.copy(inputPinyin = "")
        assert(noPinyin.pronunciation.pinyin == "")
        assert(noPinyin.pronunciation.ipa == "")
        assert(noPinyin.pronunciation.zhuyin == "")
        assert(noPinyin.pronunciation.wadeGiles == "")
      }
      it("does not accept invalid tones") {
        val badTone = example.copy(inputPinyin = "hao6")
        assert(example.pronunciation.pinyin == "hao")
        assert(example.pronunciation.ipa == "[xɑʊ̯]")
        assert(example.pronunciation.zhuyin == "ㄏㄠ")
        assert(example.pronunciation.wadeGiles == "hao")
        assert(badTone.pronunciation.tones.isEmpty)
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
        example.simplified,
        example.traditional,
        example.pronunciation,
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
        assert(example.pronunciation.ipa == "[ni] [xɑʊ̯]")
        assert(example.pronunciation.zhuyin == "ㄋㄧ ㄏㄠ")
        assert(example.pronunciation.wadeGiles == "ni hao")
      }
      it("does not break if invalid pinyin are provided") {
        val badPinyin = example.copy(inputPinyin = "invalid")
        assert(badPinyin.pronunciation.pinyin == "")
        assert(badPinyin.pronunciation.ipa == "")
        assert(badPinyin.pronunciation.zhuyin == "")
        assert(badPinyin.pronunciation.wadeGiles == "")
      }
    }
    it("does not break if there is no HSK level") {
      assert(example.hsk == HskLevel.NONE)
    }
  }
}
