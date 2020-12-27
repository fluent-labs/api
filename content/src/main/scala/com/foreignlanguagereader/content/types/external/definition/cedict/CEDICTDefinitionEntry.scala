package com.foreignlanguagereader.content.types.external.definition.cedict

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.definition.{
  ChineseDefinition,
  DefinitionSource
}
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json.{Format, Json, Reads}

case class CEDICTDefinitionEntry(
    override val subdefinitions: List[String],
    pinyin: String,
    simplified: String,
    traditional: String,
    override val token: String
) extends DefinitionEntry {
  override val tag: Option[PartOfSpeech] = None
  override val pronunciation: String = pinyin
  override val examples: Option[List[String]] = None

  override val wordLanguage: Language = Language.CHINESE
  override val definitionLanguage: Language = Language.ENGLISH
  override val source: DefinitionSource = DefinitionSource.CEDICT

  // We clearly do have simplified and traditional values so let's use them.
  override def toDefinition(partOfSpeech: PartOfSpeech): ChineseDefinition =
    ChineseDefinition(
      subdefinitions = subdefinitions,
      tag = partOfSpeech,
      examples = examples,
      inputPinyin = pinyin,
      inputSimplified = Some(simplified),
      inputTraditional = Some(traditional),
      definitionLanguage = definitionLanguage,
      source = source,
      token = token
    )
}

object CEDICTDefinitionEntry {
  // Allows serializing and deserializing in json
  implicit val format: Format[CEDICTDefinitionEntry] =
    Json.format[CEDICTDefinitionEntry]
  implicit val readsSeq: Reads[Seq[CEDICTDefinitionEntry]] =
    Reads.seq
}
