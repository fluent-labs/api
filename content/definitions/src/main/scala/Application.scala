import org.apache.spark.sql.SparkSession

object Application extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("WiktionaryParse")
    .getOrCreate()

  val wiktionaryRaw = Wiktionary
    .loadWiktionaryDump("simplewiktionary-20200301-pages-meta-current.xml")

  val wiktionaryWithSections: Unit =
    Wiktionary.getHeadings(wiktionaryRaw, 3).write.csv("triplesections")

  val simpleWiktionary =
    SimpleWiktionary.loadSimple(
      "simplewiktionary-20200301-pages-meta-current.xml"
    )
  simpleWiktionary.printSchema()
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
