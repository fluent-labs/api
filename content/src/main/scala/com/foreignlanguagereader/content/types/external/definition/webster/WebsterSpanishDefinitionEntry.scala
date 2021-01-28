package com.foreignlanguagereader.content.types.external.definition.webster

import com.foreignlanguagereader.content.formatters.WebsterFormatter
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.webster.common.WebsterPartOfSpeech.WebsterPartOfSpeech
import com.foreignlanguagereader.content.types.external.definition.webster.common._
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterSpanishDefinitionEntry(
    meta: WebsterMeta,
    headwordInfo: WebsterHeadwordInfo,
    partOfSpeechRaw: String,
    inflections: Option[Seq[WebsterInflection]],
    definitions: Seq[WebsterDefinition],
    definedRunOns: Option[Seq[WebsterDefinedRunOnPhrase]],
    shortDefinitions: Seq[String]
) extends WebsterDefinitionEntryBase
    with DefinitionEntry {

  val (wordLanguage: Language, definitionLanguage: Language) =
    meta.language match {
      case Some("en") => (Language.ENGLISH, Language.SPANISH)
      case Some("es") => (Language.SPANISH, Language.ENGLISH)
      case _          => (Language.SPANISH, Language.ENGLISH)
    }
  override val source: DefinitionSource =
    DefinitionSource.MIRRIAM_WEBSTER_SPANISH

  // Here we make some opinionated choices about how webster definitions map to our model

  // Why can't we use an enum to read this in?
  // The Spanish dictionary puts multiple pieces of information within this string.
  // eg: "masculine or feminine noun"
  override val partOfSpeech: Option[WebsterPartOfSpeech] =
    WebsterPartOfSpeech.parseFromString(partOfSpeechRaw)

  // TODO gender
  override def toDefinition(partOfSpeech: PartOfSpeech): Definition =
    wordLanguage match {
      case Language.ENGLISH =>
        DefinitionEntry.buildEnglishDefinition(this, partOfSpeech)
      case Language.SPANISH =>
        DefinitionEntry.buildSpanishDefinition(this, partOfSpeech)
      case l =>
        throw new IllegalStateException(
          s"Tried to convert a WebsterSpanishDefinitionEntry to $l"
        )
    }
}
object WebsterSpanishDefinitionEntry {
  implicit val reads: Reads[WebsterSpanishDefinitionEntry] = (
    (JsPath \ "meta").read[WebsterMeta] and
      (JsPath \ "hwi").read[WebsterHeadwordInfo] and
      (JsPath \ "fl").read[String] and
      (JsPath \ "ins")
        .readNullable[List[WebsterInflection]](
          WebsterInflection.helper.readsList
        ) and
      (JsPath \ "def")
        .read[List[WebsterDefinition]](WebsterDefinition.helper.readsList) and
      (JsPath \ "dros").readNullable[List[WebsterDefinedRunOnPhrase]](
        WebsterDefinedRunOnPhrase.helper.readsList
      ) and
      (JsPath \ "shortdef").read[List[String]](Reads.list[String])
  )((meta, hwi, fl, ins, deff, dros, shortdef) =>
    WebsterSpanishDefinitionEntry
      .apply(
        meta,
        hwi,
        WebsterFormatter.format(fl),
        ins,
        deff,
        dros,
        WebsterFormatter.formatList(shortdef)
      )
  )
  implicit val writes: Writes[WebsterSpanishDefinitionEntry] =
    Json.writes[WebsterSpanishDefinitionEntry]
  implicit val helper: JsonSequenceHelper[WebsterSpanishDefinitionEntry] =
    new JsonSequenceHelper[WebsterSpanishDefinitionEntry]
}
