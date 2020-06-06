package com.foreignlanguagereader.api.domain.definition

import java.util

import com.foreignlanguagereader.api.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.{DefinitionSource, HSKLevel, Language}
import com.foreignlanguagereader.api.HSKLevel.HSKLevel
import com.foreignlanguagereader.api.Language.Language
import play.api.libs.json.{Format, Json}

case class Definition(subdefinitions: List[String],
                      tag: String,
                      examples: List[String],
                      // These fields are needed for elasticsearch lookup
                      // But do not need to be presented to the user.
                      language: Language,
                      source: DefinitionSource,
                      token: String)

case class ChineseDefinition(subdefinitions: List[String],
                             tag: String,
                             examples: List[String],
                             pinyin: String = "",
                             simplified: String = "",
                             traditional: String = "",
                             hsk: HSKLevel = HSKLevel.NONE,
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             language: Language = Language.CHINESE,
                             source: DefinitionSource =
                               DefinitionSource.MULTIPLE,
                             token: String = "")
