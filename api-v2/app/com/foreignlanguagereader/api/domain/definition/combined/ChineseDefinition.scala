package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.HSKLevel.HSKLevel
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.{HSKLevel, Language}

case class ChineseDefinition(override val subdefinitions: List[String],
                             override val tag: String,
                             override val examples: List[String],
                             pinyin: String = "",
                             simplified: String = "",
                             traditional: String = "",
                             hsk: HSKLevel = HSKLevel.NONE,
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             override val source: DefinitionSource =
                               DefinitionSource.MULTIPLE,
                             override val token: String = "")
    extends Definition(
      subdefinitions,
      tag,
      examples,
      Language.CHINESE,
      source,
      token
    )
