package com.foreignlanguagereader.jobs.definitions

import org.scalatest.funspec.AnyFunSpec

class WiktionaryTest extends AnyFunSpec {
  describe("a wiktionary test") {
    describe("can correctly generate regexes") {
      it("can repeat a pattern") {
        assert(Wiktionary.repeat("=", 6) == "======")
      }

      describe("for a heading of any size") {
        val levelThreeHeading = Wiktionary.headingRegex(3)
        it("which match valid headings") {
          assert("=== Title ===".matches(levelThreeHeading))
        }

        it("does not match larger headings") {
          assert(!"== Title ==".matches(levelThreeHeading))
        }

        it("does not match smaller headings") {
          assert(!"==== Title ====".matches(levelThreeHeading))
        }
      }
    }
  }
}
