import com.databricks.spark.xml._
import org.apache.spark.sql.functions._
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

  val sections = Wiktionary.getSections(wiktionary)
  sections.show(5)

  val distinct = sections.select(explode(col("sections"))).distinct()
  distinct.show(500)
}
