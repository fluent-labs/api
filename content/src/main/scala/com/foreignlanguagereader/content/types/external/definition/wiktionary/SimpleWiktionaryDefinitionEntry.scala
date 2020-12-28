package com.foreignlanguagereader.content.types.external.definition.wiktionary

import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.content.types.Language.Language
import PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource

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

  override def toDefinition(partOfSpeech: PartOfSpeech): Definition =
    DefinitionEntry.buildEnglishDefinition(this, partOfSpeech)
}
