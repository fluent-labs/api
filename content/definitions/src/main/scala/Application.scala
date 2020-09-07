import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Application extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("WiktionaryParse")
    .getOrCreate()

  val wiktionaryRaw = Wiktionary
    .loadWiktionaryDump("simplewiktionary-20200301-pages-meta-current.xml")

  val wiktionaryWithSections =
    Wiktionary.extractSections(wiktionaryRaw, SimpleWiktionary.partsOfSpeech)

//  Wiktionary
//    .extractSection(wiktionaryRaw, "Noun")
//    .where("noun not like ''")
//    .write
//    .json("nouns")

  val simpleWiktionary =
    SimpleWiktionary.loadSimple(
      "simplewiktionary-20200301-pages-meta-current.xml"
    )
  simpleWiktionary
    .printSchema()
  simpleWiktionary.show(50)
  simpleWiktionary.limit(500).write.json("combined")

//  SimpleWiktionary.partsOfSpeech
//    .map(_.toLowerCase)
//    .foreach(sectionName => {
//      wiktionaryWithSections
//        .select(sectionName)
//        .where(col(sectionName) =!= "")
//        .limit(500)
//        .coalesce(1)
//        .write
//        .json(sectionName)
//    })
}
