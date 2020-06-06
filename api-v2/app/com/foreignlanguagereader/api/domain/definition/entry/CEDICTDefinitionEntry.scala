package com.foreignlanguagereader.api.domain.definition.entry

import com.foreignlanguagereader.api.{HSKLevel, Language}
import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource

case class CEDICTDefinitionEntry(override val subdefinitions: List[String],
                                 pinyin: String,
                                 simplified: String,
                                 traditional: String,
                                 override val language: Language,
                                 override val source: DefinitionSource,
                                 override val token: String)
    extends DefinitionEntry(subdefinitions, language, source, token)

object CEDICTDefinitionEntry {
  implicit def convertToDefinition(
    c: CEDICTDefinitionEntry
  ): ChineseDefinition =
    ChineseDefinition(
      c.subdefinitions,
      tag = "",
      examples = List(),
      c.pinyin,
      c.simplified,
      c.traditional,
      HSKLevel.NONE,
      DefinitionSource.CEDICT,
      c.token
    )
}
