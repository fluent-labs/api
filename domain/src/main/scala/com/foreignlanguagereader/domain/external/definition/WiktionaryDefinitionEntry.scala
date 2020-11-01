package com.foreignlanguagereader.domain.external.definition

import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json.{Format, Json, Reads}

case class WiktionaryDefinitionEntry(
    override val subdefinitions: List[String],
    pronunciation: String,
    tag: Option[PartOfSpeech],
    examples: Option[List[String]],
    override val wordLanguage: Language,
    override val definitionLanguage: Language,
    override val token: String,
    override val source: DefinitionSource = DefinitionSource.WIKTIONARY
) extends DefinitionEntry

object WiktionaryDefinitionEntry {
  // Allows serializing and deserializing in json
  implicit val format: Format[WiktionaryDefinitionEntry] =
    Json.format[WiktionaryDefinitionEntry]
  implicit val readsSeq: Reads[Seq[WiktionaryDefinitionEntry]] =
    Reads.seq(format.reads)
}
