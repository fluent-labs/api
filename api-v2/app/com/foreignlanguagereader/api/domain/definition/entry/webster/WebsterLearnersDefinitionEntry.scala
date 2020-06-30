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
import play.api.libs.json.{Json, Reads, Writes}

case class WebsterLearnersDefinitionEntry(meta: WebsterMeta,
                                          hwi: WebsterHeadwordInfo,
                                          fl: String,
                                          ins: Seq[WebsterInflection],
                                          // todo this is called def which is a keyword. Will need to manually handle.
                                          definition: Seq[WebsterDefinition])
    extends DefinitionEntry {
  // TODO figure out if we can turn this into an enum. What are the values?
  val partOfSpeech: String = fl

  val subdefinitions: List[String] = List()
  val tag: String = ""
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
  implicit val reads: Reads[WebsterLearnersDefinitionEntry] =
    Json.reads[WebsterLearnersDefinitionEntry]
  implicit val writes: Writes[WebsterLearnersDefinitionEntry] =
    Json.writes[WebsterLearnersDefinitionEntry]
  implicit val helper: JsonSequenceHelper[WebsterLearnersDefinitionEntry] =
    new JsonSequenceHelper[WebsterLearnersDefinitionEntry]()
}
