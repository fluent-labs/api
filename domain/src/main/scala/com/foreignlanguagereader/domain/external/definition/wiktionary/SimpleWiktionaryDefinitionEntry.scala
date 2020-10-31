package com.foreignlanguagereader.domain.external.definition.wiktionary

import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.external.definition.DefinitionEntry
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech.PartOfSpeech

case class SimpleWiktionaryDefinitionEntry(
    // Required fields
    token: String,
    definition: String,
    tagRaw: String,
    ipa: String,
    override val subdefinitions: List[String],
    examplesRaw: List[String],
    // Nice extras
    antonyms: List[String],
    homonyms: List[String],
    homophones: List[String],
    notes: List[String],
    otherSpellings: List[String],
    pronunciationRaw: List[String],
    related: List[String],
    synonyms: List[String],
    usage: List[String]
) extends DefinitionEntry {
  override val source: DefinitionSource =
    DefinitionSource.WIKTIONARY_SIMPLE_ENGLISH
  override val definitionLanguage: Language = Language.ENGLISH
  override val wordLanguage: Language = Language.ENGLISH
  override val pronunciation: String = pronunciationRaw.head
  override val tag: Option[PartOfSpeech] = Some(PartOfSpeech.withName(tagRaw))
  override val examples: Option[List[String]] = Some(examplesRaw)
}
