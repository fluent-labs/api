package com.foreignlanguagereader.content.formatters

import org.scalatest.funspec.AnyFunSpec

class WebsterFormatterTest extends AnyFunSpec {
  describe("A webster formatter") {
    describe("when reformatting to markdown") {
      it("correctly formats bold") {
        val original = "A {b}verb{\\/b} is a word that shows action."
        val formatted = "A **verb** is a word that shows action."
        assert(WebsterFormatter.format(original) == formatted)
      }
      it("correctly formats italics") {
        val original =
          "any of a genus {it}Trichechus{\\/it} of the family Trichechidae"
        val formatted = "any of a genus *Trichechus* of the family Trichechidae"
        assert(WebsterFormatter.format(original) == formatted)
      }
      it("correctly formats quotes") {
        val original = "{ldquo}Can I e-mail you?{rdquo}"
        val formatted = "\"Can I e-mail you?\""
        assert(WebsterFormatter.format(original) == formatted)
      }
    }
    describe("when removing garbage") {
      it("correctly removes bc") {
        val original = "{bc}a drink consisting of soda water"
        val formatted = "a drink consisting of soda water"
        assert(WebsterFormatter.format(original) == formatted)
      }
    }
  }
}
