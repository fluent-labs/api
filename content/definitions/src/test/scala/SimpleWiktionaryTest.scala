import com.foreignlanguagereader.domain.external.definition.wiktionary.SimpleWiktionaryDefinitionEntry
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import org.apache.spark.sql.SparkSession
import org.scalatest.funspec.AnyFunSpec

import scala.List

class SimpleWiktionaryTest extends AnyFunSpec {
  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("spark test example")
      .getOrCreate()
  }
  import spark.implicits._

  it("can parse a simple definition") {
    val text =
      """=== Pronunciation ===
                 |* {{IPA|/ɪz/}}
                 |* {{SAMPA|/Iz/}}
                 |* {{audio|en-us-is.ogg|Audio (US)}}
                 |
                 |== Verb ==
                 |{{verb3|be|am|is|are|was|were|been|being}}
                 |# {{Auxiliary}} {{linking verb}} A form of the [[verb]] ''[[be]]'' when talking about someone or something else.
                 |#: ''He '''is''' late for class.''
                 |#: '''''Is''' it hot in here?''
                 |
                 |=== Related words ===
                 |* [['s]] - contraction
                 |
                 |[[Category:Auxiliary verbs]]""".stripMargin

    val entryraw = WiktionaryRawEntry(42, "Is", text)
    val entryParsed: SimpleWiktionaryDefinitionEntry =
      SimpleWiktionary.parseSimple(Seq(entryraw).toDS())(spark).first()

    val definition =
      """
                        |{{verb3|be|am|is|are|was|were|been|being}}
                        |# {{Auxiliary}} {{linking verb}} A form of the [[verb]] ''[[be]]'' when talking about someone or something else.
                        |#: ''He '''is''' late for class.''
                        |#: '''''Is''' it hot in here?''
                        |
                        |=== Related words ===
                        |* [['s]] - contraction
                        |
                        |[[Category:Auxiliary verbs]]""".stripMargin

    assert(entryParsed.token == "Is")
    assert(entryParsed.definition == definition)
    assert(entryParsed.tag.contains(PartOfSpeech.VERB))
    assert(entryParsed.ipa == "ɪz")
    assert(
      entryParsed.subdefinitions === List(
        "{{Auxiliary}} {{linking verb}} A form of the [[verb]] ''[[be]]'' when talking about someone or something else."
      )
    )
    assert(
      entryParsed.examples.contains(
        List(
          "''He '''is''' late for class.''",
          "'''''Is''' it hot in here?''"
        )
      )
    )
    assert(
      entryParsed.pronunciation === "=\n* {{IPA|/ɪz/}}\n* {{SAMPA|/Iz/}}\n* {{audio|en-us-is.ogg|Audio (US)}}\n\n"
    )
    assert(
      entryParsed.related sameElements Array(
        "=\n* [['s]] - contraction\n\n[[Category:Auxiliary verbs]]"
      )
    )

    assert(entryParsed.synonyms.isEmpty)
    assert(entryParsed.antonyms.isEmpty)
    assert(entryParsed.usage.isEmpty)
    assert(entryParsed.notes.isEmpty)
    assert(entryParsed.homophones.isEmpty)
    assert(entryParsed.homonyms.isEmpty)
    assert(entryParsed.otherSpellings.isEmpty)
  }
}
