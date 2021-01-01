package com.foreignlanguagereader.dto.v1.definition.chinese

import play.api.libs.json.{Format, Json}

case class ChinesePronunciation(
    pinyin: String = "",
    ipa: String = "",
    zhuyin: String = "",
    wadeGiles: String = "",
    tones: Seq[String] = List()
) {
  def +(that: ChinesePronunciation): ChinesePronunciation =
    ChinesePronunciation(
      this.pinyin + " " + that.pinyin,
      this.ipa + " " + that.ipa,
      this.zhuyin + " " + that.zhuyin,
      this.wadeGiles + " " + that.wadeGiles,
      this.tones ++ that.tones
    )
}
object ChinesePronunciation {
  implicit val format: Format[ChinesePronunciation] = Json.format
}
