package com.foreignlanguagereader.api.controller.v1.graphql.queries

import org.scalatest.funsuite.AnyFunSuite
import sangria.macros._

class DefinitionQueryTest extends AnyFunSuite {
  test("it can query all fields in a generic definition") {
    graphql"""
      {
        definition(wordLanguage: ENGLISH, definitionLanguage: ENGLISH, token:"test") {
            ...on GenericDefinitionDTO {
                subdefinitions
                tag
                examples
            }
        }
    }
    """
  }
  test("it can query all fields in a chinese definition") {
    graphql"""
      {
        definition(wordLanguage: CHINESE, definitionLanguage: ENGLISH, token:"test") {
            ...on ChineseDefinitionDTO {
                subdefinitions
                tag
                examples
                simplified
                traditional
                pronunciation {
                  pinyin
                  ipa
                  zhuyin
                  wadeGiles
                  tones
                }
                hsk
            }
        }
    }
    """
  }
}
