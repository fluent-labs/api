package com.foreignlanguagereader.domain.content.chinese

import com.foreignlanguagereader.domain.util.ContentFileLoader
import com.foreignlanguagereader.dto.v1.definition.chinese.ChinesePronunciation
import play.api.libs.json.{Json, Reads}

object ChinesePronunciationGenerator {
  val toneRegex = "[12345]+"

  private[this] val pronunciations: Map[String, ChinesePronunciationFromFile] =
    ContentFileLoader
      .loadJsonResourceFile[Seq[ChinesePronunciationFromFile]](
        "/definition/chinese/pronunciation.json"
      )
      .map(pron => pron.pinyin -> pron)
      .toMap

  // This tags the words with zhuyin, wade giles, and IPA based on the pinyin.
  // It also pulls the tones out of the pinyin as a separate thing
  // This works because pinyin is a perfect sound system
  def getPronunciation(pinyin: String): ChinesePronunciation = {
    val (rawPinyin, tones) = separatePinyinFromTones(pinyin)

    // We don't want to drop any because tone and pinyin must line up.
    // If any part of the input is garbage then the whole thing should be treated as such.
    val pronunciation = {
      val temp = rawPinyin.map(pinyin => pronunciations.get(pinyin))
      if (temp.forall(_.isDefined)) Some(temp.flatten) else None
    }

    (pronunciation, tones) match {
      case (Some(p), Some(t)) =>
        p.zip(t)
          .map {
            case (pron, tone) => pron.toDomain(List(tone))
          }
          .reduce(_ + _)
      case (Some(p), None) =>
        p.map(_.toDomain()).reduce(_ + _)
      case _ => ChinesePronunciation()
    }
  }

  // Pulling tone numbers off pinyin turns out to be complicated
  // This returns the pinyin with all tones stripped,
  private[this] def separatePinyinFromTones(
      pinyin: String
  ): (Array[String], Option[Array[String]]) = {
    pinyin.split(" ") match {
      case hasTones if hasTones.forall(_.takeRight(1).matches(toneRegex)) =>
        (hasTones.map(_.dropRight(1)), Some(hasTones.map(_.takeRight(1))))
      // Specifically remove all tone marks from the pinyin.
      // Otherwise it will attempt to convert pinyin to other pronunciation with words in, which will fail
      case hasBadTones
          if hasBadTones.exists(_.takeRight(1).matches(toneRegex)) =>
        // We need to remove the last number, but there might be numbers within. Eg; 2B
        (
          hasBadTones.map(pinyin =>
            if (pinyin.takeRight(1).matches("[0-9]")) pinyin.dropRight(1)
            else pinyin
          ),
          None
        )
      case noTones => (noTones, None)
    }
  }
}

case class ChinesePronunciationFromFile(
    pinyin: String,
    ipa: String,
    zhuyin: String,
    wadeGiles: String
) {
  def toDomain(tones: List[String] = List()): ChinesePronunciation =
    ChinesePronunciation(pinyin, ipa, zhuyin, wadeGiles, tones)
}
object ChinesePronunciationFromFile {
  implicit val reads: Reads[ChinesePronunciationFromFile] =
    Json.reads[ChinesePronunciationFromFile]
  implicit val readsSeq: Reads[Seq[ChinesePronunciationFromFile]] =
    Reads.seq(reads)
}
