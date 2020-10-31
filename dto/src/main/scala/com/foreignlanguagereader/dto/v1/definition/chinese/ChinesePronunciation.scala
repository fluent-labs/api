package com.foreignlanguagereader.dto.v1.definition.chinese

import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{ObjectTypeDescription, deriveObjectType}
import sangria.schema

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
  implicit val graphQlType: schema.ObjectType[Unit, ChinesePronunciation] =
    deriveObjectType[Unit, ChinesePronunciation](
      ObjectTypeDescription(
        "A holder of different pronunciation formats for Chinese words"
      )
    )
}
