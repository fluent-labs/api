package com.foreignlanguagereader.api.domain.definition.entry.webster

import com.foreignlanguagereader.api.domain.definition.entry.webster.WebsterSource.WebsterSource
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

object WebsterSource extends Enumeration {
  type WebsterSource = Value

  val LEARNERS: Value = Value("learners")
  val SPANISH: Value = Value("spanish")

  implicit val reads: Reads[WebsterSource] = Reads.enumNameReads(WebsterSource)
  implicit val writes: Writes[WebsterSource] = Writes.enumNameWrites
}

case class WebsterMeta(id: String,
                       uuid: String,
                       sort: String,
                       src: WebsterSource,
                       section: String,
                       stems: Seq[String],
                       offensive: Boolean)
object WebsterMeta {
  implicit val format: Format[WebsterMeta] = Json.format[WebsterMeta]
}

case class HeadwordInfo(headword: String,
                        pronunciations: Option[Seq[WebsterPronunciation]])
object HeadwordInfo {
  implicit val writes: Writes[HeadwordInfo] = Json.writes[HeadwordInfo]
  implicit val reads: Reads[HeadwordInfo] = ((JsPath \ "hw")
    .read[String] and (JsPath \ "prs").readNullable[Seq[WebsterPronunciation]])(
    HeadwordInfo.apply _
  )
}

case class WebsterPronunciation(writtenPronunciation: Option[String],
                                beforePronunciationLabel: Option[String],
                                afterPronunciationLabel: Option[String],
                                seperatorPunctuation: Option[String],
                                sound: Option[WebsterPronunciationSound])
object WebsterPronunciation {
  implicit val reads: Reads[WebsterPronunciation] = (
    (JsPath \ "mw").readNullable[String] and
      (JsPath \ "l").readNullable[String] and
      (JsPath \ "l2").readNullable[String] and
      (JsPath \ "pun").readNullable[String] and
      (JsPath \ "sound").readNullable[WebsterPronunciationSound]
  )(WebsterPronunciation.apply _)
  implicit val readsSeq: Reads[Seq[WebsterPronunciation]] = Reads.seq(reads)
  implicit val writes: Writes[WebsterPronunciation] = (
    (JsPath \ "writtenPronunciation").writeNullable[String] and
      (JsPath \ "beforePronunciationLabel").writeNullable[String] and
      (JsPath \ "afterPronunciationLabel").writeNullable[String] and
      (JsPath \ "seperatorPunctuation").writeNullable[String] and
      (JsPath \ "sound").writeNullable[WebsterPronunciationSound]
  )(unlift(WebsterPronunciation.unapply))
  implicit val writesSeq: Writes[Seq[WebsterPronunciation]] = Writes.seq(writes)
}

case class WebsterPronunciationSound(audio: String,
                                     language: String = "en",
                                     country: String = "us") {
  val subdirectory: String = audio match {
    case bix if bix.startsWith("bix")                      => "bix"
    case gg if gg.startsWith("gg")                         => "gg"
    case number if number.matches("^[0-9\\.?!_\\-,;:]+.*") => "number"
    case _                                                 => audio.charAt(0).toString
  }

  // TODO maybe look at automatically setting this
  // For spanish change this to "es"
//  val language = "en"
  // For spanish definitions change this to "me" Depends on "lang": "es" in the metadata
//  val country = "us"

  val audioUrl: String =
    s"https://media.merriam-webster.com/audio/prons/$language/$country/mp3/$subdirectory/$audio.mp3"
}
object WebsterPronunciationSound {
  def createWithDefaults(audio: String, ref: String) =
    WebsterPronunciationSound(audio)
  implicit val reads: Reads[WebsterPronunciationSound] = (
    (JsPath \ "audio").read[String] and
      (JsPath \ "ref").read[String]
  )(WebsterPronunciationSound.createWithDefaults _)
  implicit val writes: Writes[WebsterPronunciationSound] =
    Json.writes[WebsterPronunciationSound]

case class WebsterInflection(inflection: Option[String],
                             ifc: Option[String],
                             il: Option[String],
                             prs: Option[WebsterPronunciation])
object WebsterInflection {
  implicit val format: Format[WebsterInflection] =
    Json.format[WebsterInflection]
}