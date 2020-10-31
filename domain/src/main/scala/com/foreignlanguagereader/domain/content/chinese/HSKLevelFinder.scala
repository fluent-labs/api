package com.foreignlanguagereader.domain.content.chinese

import com.foreignlanguagereader.domain.util.ContentFileLoader
import com.foreignlanguagereader.dto.v1.definition.chinese.HskLevel
import com.foreignlanguagereader.dto.v1.definition.chinese.HskLevel.HSKLevel
import com.github.houbb.opencc4j.util.ZhConverterUtil
import play.api.libs.json.{Json, Reads}

object HSKLevelFinder {
  private[this] val hsk: HskHolder = ContentFileLoader
    .loadJsonResourceFile[HskHolder]("/definition/chinese/hsk.json")

  def sentenceIsTraditional(sentence: String): Boolean =
    ZhConverterUtil.isTraditional(sentence)

  def getHSK(simplified: String): HSKLevel = hsk.getLevel(simplified)
}

case class HskHolder(
    hsk1: Set[String],
    hsk2: Set[String],
    hsk3: Set[String],
    hsk4: Set[String],
    hsk5: Set[String],
    hsk6: Set[String]
) {
  // scalastyle:off cyclomatic.complexity
  def getLevel(simplified: String): HskLevel.Value =
    simplified match {
      case s if hsk1.contains(s) => HskLevel.ONE
      case s if hsk2.contains(s) => HskLevel.TWO
      case s if hsk3.contains(s) => HskLevel.THREE
      case s if hsk4.contains(s) => HskLevel.FOUR
      case s if hsk5.contains(s) => HskLevel.FIVE
      case s if hsk6.contains(s) => HskLevel.SIX
      case _                     => HskLevel.NONE
    }
  // scalastyle:on cyclomatic.complexity
}
object HskHolder {
  implicit val reads: Reads[HskHolder] = Json.reads[HskHolder]
}
