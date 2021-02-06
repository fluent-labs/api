package com.foreignlanguagereader.content.difficulty

import com.foreignlanguagereader.content.difficulty.CEFRDifficultyLevel.CEFRDifficultyLevel

trait DifficultyFinder[T] {
  def getDifficulty(token: String): T
  def convertDifficultyToCEFR(level: T): CEFRDifficultyLevel

  def getDifficultyAsCEFR(token: String): CEFRDifficultyLevel =
    convertDifficultyToCEFR(getDifficulty(token))
}
