import org.apache.spark.sql.SparkSession

object Application extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("WiktionaryParse")
    .getOrCreate()

  val simpleWiktionary =
    Wiktionary.loadSimple("simplewiktionary-20200301-pages-meta-current.xml")
  simpleWiktionary.write.json("simple")
}
