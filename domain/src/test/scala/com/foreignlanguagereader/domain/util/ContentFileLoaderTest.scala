package com.foreignlanguagereader.domain.util

import com.foreignlanguagereader.domain.content.chinese.{
  ChinesePronunciationFromFile,
  HskHolder
}
import org.scalatest.funspec.AnyFunSpec

class ContentFileLoaderTest extends AnyFunSpec {
  describe("A content file loader") {
    it("can load files from json") {
      val goodFile = ContentFileLoader
        .loadJsonResourceFile[Seq[ChinesePronunciationFromFile]](
          "/definition/chinese/pronunciation.json"
        )
      assert(goodFile.nonEmpty)
    }
    it("throws an exception if the file cannot be opened") {
      assertThrows[NullPointerException] {
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
            "/definition/chinese/pronunciation.json"
          )
      }
    }
  }
}
