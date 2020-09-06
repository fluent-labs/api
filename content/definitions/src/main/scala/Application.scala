import org.apache.spark.sql.{DataFrame, SparkSession}

object Application extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("WiktionaryParse")
    .getOrCreate()

  val wiktionary: DataFrame =
    Wiktionary.loadWiktionaryDump(
      "simplewiktionary-20200301-pages-meta-current.xml"
    )

  wiktionary.write.json("wiktionary")
}
