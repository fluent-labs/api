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

        // There's some subtle bugs around matching too many and too few
        // This is to prevent regression

        it("does not match larger headings") {
          assert(!"== Title ==".matches(levelThreeHeading))
        }

        it("does not match smaller headings") {
          assert(!"==== Title ====".matches(levelThreeHeading))
        }
      }

      // This is there to cover refactors, feel free to wipe the assertion if the regex materially changes.
      it("for a section") {
        assert(
          "(?s)(?i)== *MyTestSection *==(.*?)(?>(?>== *[A-Za-z0-9]+ *==[ |\n]+)|\\Z)+" == Wiktionary
            .sectionRegex("MyTestSection")
        )
      }

      // This is there to cover refactors, feel free to wipe the assertion if the regex materially changes.
      it("for a subsection") {
        assert(
          "(?s)(?i)=== *MyTestSubsection *===(.*?)(?>(?>== *[A-Za-z0-9]+ *==[ |\n]+)|\\Z)+" == Wiktionary
            .subSectionRegex("MyTestSubsection")
        )
      }
    }
  }
}
