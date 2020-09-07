import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Application extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("WiktionaryParse")
    .getOrCreate()

//  exploreSections(
//    "simplewiktionary-20200301-pages-meta-current.xml",
//    explorationSubsections
//  )

  // Use this when you want to know what kind of sections a backup has. Good for getting the rough structure of the dump
  def findSections(backupFilePath: String,
                   sectionCount: Int,
                   outputFileName: String): Unit = {
    val wiktionaryRaw = Wiktionary
      .loadWiktionaryDump(backupFilePath)
    Wiktionary
      .getHeadings(wiktionaryRaw, sectionCount)
      .write
      .csv(outputFileName)
  }

  // Use this when you want to see what is in each section you found up above
  // eg: is it common? Do I care about what's in it?
  def exploreSections(backupFilePath: String,
                      sectionNames: List[String]): Unit = {
    val wiktionaryRaw = Wiktionary
      .loadWiktionaryDump(backupFilePath)
    sectionNames.foreach(sectionName => {
      Wiktionary
        .extractSubSection(wiktionaryRaw, sectionName)
        .select("text", sectionName)
        .where(col(sectionName) =!= "")
        .limit(500)
        .coalesce(1)
        .write
        .json(sectionName)
    })
  }
}
