package com.foreignlanguagereader.api.contentsource.definition.webster

import com.foreignlanguagereader.api.contentsource.definition.DefinitionEntry
import com.foreignlanguagereader.api.contentsource.definition.webster.common.WebsterPartOfSpeech.WebsterPartOfSpeech
import com.foreignlanguagereader.api.contentsource.definition.webster.common._
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterLearnersDefinitionEntry(
  meta: WebsterMeta,
  headwordInfo: WebsterHeadwordInfo,
  partOfSpeech: Option[WebsterPartOfSpeech],
  inflections: Option[Seq[WebsterInflection]],
  definitions: Seq[WebsterDefinition],
  definedRunOns: Option[Seq[WebsterDefinedRunOnPhrase]],
  shortDefinitions: Seq[String]
) extends WebsterDefinitionEntryBase
    with DefinitionEntry {
  override val wordLanguage: Language = Language.ENGLISH
  override val definitionLanguage: Language = Language.ENGLISH
  override val source: DefinitionSource =
    DefinitionSource.MIRRIAM_WEBSTER_LEARNERS

  // TODO Usage labels https://www.merriam-webster.com/help/explanatory-notes/dict-usage
  // Might be worth noting if a word is obsolete
  // Or non-standard
  // Learners may not even want to see obsolete words
  // And definitely should be discouraged from adding them to their vocabulary list.
}
object WebsterLearnersDefinitionEntry {
  implicit val reads: Reads[WebsterLearnersDefinitionEntry] = (
    (JsPath \ "meta").read[WebsterMeta] and
      (JsPath \ "hwi").read[WebsterHeadwordInfo] and
      (JsPath \ "fl").readNullable[WebsterPartOfSpeech] and
      (JsPath \ "ins")
        .readNullable[Seq[WebsterInflection]](WebsterInflection.helper.readsSeq) and
      (JsPath \ "def")
        .read[Seq[WebsterDefinition]](WebsterDefinition.helper.readsSeq) and
      (JsPath \ "dros").readNullable[Seq[WebsterDefinedRunOnPhrase]](
        WebsterDefinedRunOnPhrase.helper.readsSeq
      ) and
      (JsPath \ "shortdef").read[Seq[String]](Reads.seq[String])
  )(WebsterLearnersDefinitionEntry.apply _)
  implicit val writes: Writes[WebsterLearnersDefinitionEntry] =
    Json.writes[WebsterLearnersDefinitionEntry]
  implicit val helper: JsonSequenceHelper[WebsterLearnersDefinitionEntry] =
    new JsonSequenceHelper[WebsterLearnersDefinitionEntry]
}
