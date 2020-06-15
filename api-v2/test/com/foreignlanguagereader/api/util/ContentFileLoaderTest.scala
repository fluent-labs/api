package com.foreignlanguagereader.api.util

import com.foreignlanguagereader.api.service.definition.{
  ChinesePronunciationFromFile,
  HskHolder
}
import org.scalatest.funspec.AnyFunSpec

class ContentFileLoaderTest extends AnyFunSpec {
  describe("A content file loader") {
    it("can load files from json") {
      val goodFile = ContentFileLoader
        .loadJsonResourceFile[Seq[ChinesePronunciationFromFile]](
          "/resources/definition/chinese/pronunciation.json"
        )
      assert(goodFile.nonEmpty)
    }
    it("throws an exception if the file cannot be opened") {
      assertThrows[NullPointerException] {
        ContentFileLoader
          .loadJsonResourceFile[Seq[ChinesePronunciationFromFile]](
            "/resources/notfound.json"
          )
      }
    }
    it("throws an exception if the file is invalid") {
      assertThrows[IllegalStateException] {
        ContentFileLoader
          .loadJsonResourceFile[HskHolder](
            "/resources/definition/chinese/pronunciation.json"
          )
      }
    }
  }
}
