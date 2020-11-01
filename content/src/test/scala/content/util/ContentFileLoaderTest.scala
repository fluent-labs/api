package content.util

import com.foreignlanguagereader.content.enrichers.chinese.{
  ChinesePronunciationFromFile,
  HskHolder
}
import com.foreignlanguagereader.content.util.ContentFileLoader
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
            "/chinese/pronunciation.json"
          )
      }
    }
  }
}
