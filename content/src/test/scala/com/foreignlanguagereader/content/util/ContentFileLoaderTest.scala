package com.foreignlanguagereader.content.util

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.foreignlanguagereader.content.difficulty.chinese.hsk.HskHolder
import com.foreignlanguagereader.content.enrichers.chinese.ChinesePronunciationFromFile
import org.scalatest.funspec.AnyFunSpec

class ContentFileLoaderTest extends AnyFunSpec {
  describe("A content file loader") {
    it("can load files from json") {
      val goodFile = ContentFileLoader
        .loadJsonResourceFile[Seq[ChinesePronunciationFromFile]](
          "/chinese/pronunciation.json"
        )
      assert(goodFile.nonEmpty)
    }
    it("throws an exception if the file cannot be opened") {
      assertThrows[MismatchedInputException] {
        ContentFileLoader
          .loadJsonResourceFile[Seq[ChinesePronunciationFromFile]](
            "=/notfound.json"
          )
      }
    }
    it("throws an exception if the file is invalid") {
      assertThrows[IllegalStateException] {
        ContentFileLoader
          .loadJsonResourceFile[HskHolder](
            "/chinese/pronunciation.json"
          )
      }
    }
  }
}
