package com.foreignlanguagereader.domain.external.definition.webster.common

import com.foreignlanguagereader.domain.util.JsonSequenceHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WebsterPronunciation(
    writtenPronunciation: Option[String],
    beforePronunciationLabel: Option[String],
    afterPronunciationLabel: Option[String],
    seperatorPunctuation: Option[String],
    sound: Option[WebsterPronunciationSound],
    ipa: Option[String]
)
object WebsterPronunciation {
  implicit val reads: Reads[WebsterPronunciation] = (
    (JsPath \ "mw").readNullable[String] and
      (JsPath \ "l").readNullable[String] and
      (JsPath \ "l2").readNullable[String] and
      (JsPath \ "pun").readNullable[String] and
      (JsPath \ "sound")
        .readNullable[WebsterPronunciationSound] and
      (JsPath \ "ipa").readNullable[String]
  )(WebsterPronunciation.apply _)
  implicit val writes: Writes[WebsterPronunciation] =
    Json.writes[WebsterPronunciation]
  implicit val helper: JsonSequenceHelper[WebsterPronunciation] =
    new JsonSequenceHelper[WebsterPronunciation]
}

case class WebsterPronunciationSound(
    audio: String,
    language: String = "en",
    country: String = "us"
) {
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
  def createWithDefaults(
      audio: String,
      ref: Option[String]
  ): WebsterPronunciationSound =
    WebsterPronunciationSound(audio)
  implicit val reads: Reads[WebsterPronunciationSound] = (
    (JsPath \ "audio").read[String] and
      (JsPath \ "ref").readNullable[String]
  )(WebsterPronunciationSound.createWithDefaults _)
  implicit val writes: Writes[WebsterPronunciationSound] =
    Json.writes[WebsterPronunciationSound]
  implicit val helper: JsonSequenceHelper[WebsterPronunciationSound] =
    new JsonSequenceHelper[WebsterPronunciationSound]

}
