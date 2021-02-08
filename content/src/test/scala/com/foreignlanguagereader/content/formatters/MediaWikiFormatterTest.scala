package com.foreignlanguagereader.content.formatters

import org.scalatest.funspec.AnyFunSpec

class MediaWikiFormatterTest extends AnyFunSpec {
  describe("A MediaWiki formatter") {
    describe("when reformatting to markdown") {
      describe("with emphasis markers") {
        it("correctly formats bold") {
          val original = "A '''verb''' is a word that shows action."
          val formatted = "A **verb** is a word that shows action."
          assert(MediaWikiFormatter.format(original) == formatted)
        }
        it("correctly formats italics") {
          val original =
            "any of a genus ''Trichechus'' of the family Trichechidae"
          val formatted =
            "any of a genus *Trichechus* of the family Trichechidae"
          assert(MediaWikiFormatter.format(original) == formatted)
        }
        it("correctly formats bold and italics") {
          val original = "'''''Can I e-mail you?'''''"
          val formatted = "***Can I e-mail you?***"
          assert(MediaWikiFormatter.format(original) == formatted)
        }
      }
      it("with headings") {
        (1 to 6).map(n => {
          val original = "=".repeat(n) + "text" + "=".repeat(n)
          val formatted = "#".repeat(n) + " text"
          assert(MediaWikiFormatter.format(original) == formatted)
        })
      }
    }
  }
}
