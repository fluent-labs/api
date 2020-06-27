package com.foreignlanguagereader.api.domain.definition.entry.webster

import play.api.libs.json._
import play.api.libs.json.Reads._

case class WebsterMeta(id: String,
                       uuid: String,
                       sort: String,
                       src: String,
                       section: String,
                       stems: Seq[String],
                       offensive: Boolean)
object WebsterMeta {
  implicit val format: Format[WebsterMeta] = Json.format[WebsterMeta]
}

case class HeadwordInfo(hw: String, prs: Option[Seq[WebsterPronunciation]])
object HeadwordInfo {
  implicit val format: Format[HeadwordInfo] = Json.format[HeadwordInfo]
}

case class WebsterPronunciation(mw: Option[String],
                                l: Option[String],
                                l2: Option[String],
                                pun: Option[String],
                                sound: Option[WebsterPronunciationSound])
object WebsterPronunciation {
  implicit val format: Format[WebsterPronunciation] =
    Json.format[WebsterPronunciation]
}

case class WebsterPronunciationSound(audio: String, ref: String, stat: String) {
  val subdirectory: String = audio match {
    case bix if bix.startsWith("bix")                      => "bix"
    case gg if gg.startsWith("gg")                         => "gg"
    case number if number.matches("^[0-9\\.?!_\\-,;:]+.*") => "number"
    case _                                                 => audio.charAt(0).toString
  }

  // For spanish change this to "es"
  val language = "en"
  // For spanish definitions change this to "me" Depends on "lang": "es" in the metadata
  val country = "us"

  val audioUrl: String =
    s"https://media.merriam-webster.com/audio/prons/$language/$country/mp3/$subdirectory/$audio.mp3"
}
object WebsterPronunciationSound {
  implicit val format: Format[WebsterPronunciationSound] =
    Json.format[WebsterPronunciationSound]
}

case class WebsterInflection(inflection: Option[String],
                             ifc: Option[String],
                             il: Option[String],
                             prs: Option[WebsterPronunciation])
object WebsterInflection {
  implicit val format: Format[WebsterInflection] =
    Json.format[WebsterInflection]
}
