package com.foreignlanguagereader.api.domain.definition.entry

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource

case class WiktionaryDefinitionEntry(override val subdefinitions: List[String],
                                     tag: String,
                                     examples: List[String],
                                     override val language: Language,
                                     override val source: DefinitionSource,
                                     override val token: String)
    extends DefinitionEntry(subdefinitions, language, source, token)

object WiktionaryDefinitionEntry {
  implicit def convertToDefinition(w: WiktionaryDefinitionEntry): Definition =
    Definition(
      w.subdefinitions,
      w.tag,
      w.examples,
      w.language,
      DefinitionSource.WIKTIONARY,
      w.token
    )
}
