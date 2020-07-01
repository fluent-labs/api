package com.foreignlanguagereader.api.domain.definition.entry.webster

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.webster.common._
import com.foreignlanguagereader.api.domain.definition.entry.{
  DefinitionEntry,
  DefinitionSource
}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterLearnersDefinitionEntry(
  meta: WebsterMeta,
  headwordInfo: WebsterHeadwordInfo,
  // TODO figure out if we can turn this into an enum. What are the values?
  tag: String,
  inflections: Seq[WebsterInflection],
  definition: Seq[WebsterDefinition],
  definedRunOns: Seq[WebsterDefinedRunOnPhrase],
  shortDefinitions: Seq[String]
) extends DefinitionEntry {
  val subdefinitions: List[String] = List()
  val examples: List[String] = List()
  val token: String = ""

  override val wordLanguage: Language = Language.ENGLISH
  override val definitionLanguage: Language = Language.ENGLISH
  override val source: DefinitionSource =
    DefinitionSource.MIRRIAM_WEBSTER_LEARNERS

  lazy override val toDefinition: Definition = Definition(
    subdefinitions,
    tag,
    examples,
    wordLanguage,
    definitionLanguage,
    source,
    token
  )
}
object WebsterLearnersDefinitionEntry {
  implicit val reads: Reads[WebsterLearnersDefinitionEntry] = (
    (JsPath \ "meta").read[WebsterMeta] and
      (JsPath \ "hwi").read[WebsterHeadwordInfo] and
      (JsPath \ "fl").read[String] and
      (JsPath \ "ins")
        .read[Seq[WebsterInflection]](WebsterInflection.helper.readsSeq) and
      (JsPath \ "def")
        .read[Seq[WebsterDefinition]](WebsterDefinition.helper.readsSeq) and
      (JsPath \ "dros").read[Seq[WebsterDefinedRunOnPhrase]](
        WebsterDefinedRunOnPhrase.helper.readsSeq
      ) and
      (JsPath \ "shortdef").read[Seq[String]](Reads.seq[String])
  )(WebsterLearnersDefinitionEntry.apply _)
  implicit val writes: Writes[WebsterLearnersDefinitionEntry] =
    Json.writes[WebsterLearnersDefinitionEntry]
  implicit val helper: JsonSequenceHelper[WebsterLearnersDefinitionEntry] =
    new JsonSequenceHelper[WebsterLearnersDefinitionEntry]()
}
