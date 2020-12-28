package com.foreignlanguagereader.content.enrichers.chinese

import com.foreignlanguagereader.content.util.ContentFileLoader
import com.foreignlanguagereader.dto.v1.definition.chinese.HSKLevel
import com.github.houbb.opencc4j.util.ZhConverterUtil
import play.api.libs.json.{Json, Reads}

object HSKLevelFinder {
  private[this] val hsk: HskHolder = ContentFileLoader
    .loadJsonResourceFile[HskHolder]("/chinese/hsk.json")

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
  def getLevel(simplified: String): HSKLevel =
    simplified match {
      case s if hsk1.contains(s) => HSKLevel.ONE
      case s if hsk2.contains(s) => HSKLevel.TWO
      case s if hsk3.contains(s) => HSKLevel.THREE
      case s if hsk4.contains(s) => HSKLevel.FOUR
      case s if hsk5.contains(s) => HSKLevel.FIVE
      case s if hsk6.contains(s) => HSKLevel.SIX
      case _                     => HSKLevel.NONE
    }
  // scalastyle:on cyclomatic.complexity
}
object HskHolder {
  implicit val reads: Reads[HskHolder] = Json.reads[HskHolder]
}
