package com.foreignlanguagereader.content.difficulty.chinese.hsk

import com.foreignlanguagereader.content.difficulty.CEFRDifficultyLevel.CEFRDifficultyLevel
import com.foreignlanguagereader.content.difficulty.{
  CEFRDifficultyLevel,
  DifficultyFinder
}
import com.foreignlanguagereader.content.util.ContentFileLoader
import com.foreignlanguagereader.dto.v1.definition.chinese.HSKLevel

object HSKDifficultyFinder extends DifficultyFinder[HSKLevel] {
  private[this] val hsk: HskHolder = ContentFileLoader
    .loadJsonResourceFile[HskHolder]("/chinese/hsk.json")

  override def getDifficulty(token: String): HSKLevel = hsk.getLevel(token)
  override def convertDifficultyToCEFR(level: HSKLevel): CEFRDifficultyLevel =
    level match {
      case HSKLevel.ONE   => CEFRDifficultyLevel.A1_BEGINNING
      case HSKLevel.TWO   => CEFRDifficultyLevel.A2_ELEMENTARY
      case HSKLevel.THREE => CEFRDifficultyLevel.B1_INTERMEDIATE
      case HSKLevel.FOUR  => CEFRDifficultyLevel.B2_UPPER_INTERMEDIATE
      case HSKLevel.FIVE  => CEFRDifficultyLevel.C1_ADVANCED
      case HSKLevel.SIX   => CEFRDifficultyLevel.C2_PROFICIENCY
      case HSKLevel.NONE  => CEFRDifficultyLevel.C2_PROFICIENCY
    }
}
