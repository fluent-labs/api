import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import com.databricks.spark.xml._

class Wiktionary(implicit spark: SparkSession) {
  import spark.implicits._

  val simpleWiktionaryBucket =
    "s3a://foreign-language-reader-definitions/wiktionary/simple/"
  val ignoredTitles: Set[String] =
    Set("MediaWiki", "Template", "Wiktionary", "User", "Category")

  def loadWiktionaryDump(path: String): DataFrame =
    spark.read
      .option("rowTag", "page")
      .xml(path)
      .filter(row => filterMetaArticles(row))
      .select("revision.text", "title")

  def loadSimple(filename: String): DataFrame =
    loadWiktionaryDump(simpleWiktionaryBucket + filename)

  def filterMetaArticles(row: Row): Boolean = {
    val title = row.getAs[String]("title")
    ignoredTitles.forall(prefix => title.startsWith(prefix))
  }

  val cat =
    """{{BE850}}
    |{{BNC1000HW}}
    |
    |=== Pronunciation ===
    |* {{enPR|/kăt/}}
    |* {{IPA|/kæt/}}
    |* {{SAMPA|/k{t/}}
    |* {{audio|En-us-cat.ogg|Audio (US)}}
    |* {{audio|En-us-inlandnorth-cat.ogg|Audio (US-Inland North)}}
    |* {{audio|En-uk-a cat.ogg|Audio (UK)}}
    |
    |== Noun ==
    |{{noun}}
    |[[File:Miaulementcornisg.ogg|right]]
    |[[File:Cat purring.ogg|right]]
    |[[File:Cat03.jpg|thumb|A cat]]
    |# {{countable}} A '''cat''' is a [[domestic]] [[animal]] often kept as a [[pet]]; it has [[whisker]]s and likes to chase [[mice]]. {{synonyms|puss|pussy|feline}}
    |#: ''Our pet '''cat''' has just had kittens.''
    |# '''Cats''' refers to the [[family]] of many different [[wild]] animals that are related to the domestic '''cat'''.
    |#:''[[lion|Lion]]s and [[tiger]]s are big '''cat'''s.''
    |# A '''cat''' is a short-form for a [[catfish]].
    |# {{slang}} A '''cat''' refers to a person who is a [[prostitute]].
    |# A '''cat''' refers to a [[catamaran]].
    |# {{computing}} The '''cat''' command, a {{w|Unix}} [[computer]] [[program]] used to [[read]] one or more [[file]]s and [[output]] its contents.
    |# {{military}} '''Cat''' is a short-form for a [[catapult]].
    |
    |=== Related words ===
    |* [[caterwaul]]
    |* [[catty]]
    |* [[cathead]]
    |* [[catboat]]
    |* [[catwalk]]
    |* [[catfish]]
    |* [[copycat]]
    |* [[catlike]]
    |
    |=== See also ===
    |* [[category]]
    |* [[caterpillar]]
    |* [[kitten]]
    |
    |== Verb ==
    |{{verb|cat|t}}
    |# {{nautical}} If you '''cat''' an [[anchor]], you lift it onto the [[cathead]].
    |# {{slang}} If you '''cat''', you [[vomit]] out something.
    |# {{computing}} When you '''cat''', you apply the [[#Noun|cat]] command on a Unix computer.
    |
    |== Adjective ==
    |{{adjective}}
    |# {{informal}} {{context|Ireland}} When something is '''cat''', it is [[terrible]] and not [[good]].
    |#: ''The weather these few days has been '''cat''', so we decided to cancel our outing to the beach.''
    |
    |{{commonscat|cats}}
    |
    |[[Category:Mammals]]""".stripMargin
}
