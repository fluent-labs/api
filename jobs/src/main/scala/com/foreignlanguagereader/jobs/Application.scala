package com.foreignlanguagereader.jobs

import com.foreignlanguagereader.jobs.definitions.Wiktionary
import com.foreignlanguagereader.jobs.definitions.source.SimpleWiktionary
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Application extends App {
  val SIMPLE_WIKTIONARY_PATH =
    "content/definitions/src/main/resources/simplewiktionary-20200301-pages-meta-current.xml"

  implicit val spark: SparkSession = SparkSession.builder
    .master("local[*]")
    .appName("WiktionaryParse")
    .getOrCreate()

  val simpleWiktionary = SimpleWiktionary.loadFromPath(SIMPLE_WIKTIONARY_PATH)
  simpleWiktionary.limit(500).coalesce(1).write.json("simple")

  // Use this when you want to see what is in each section you found up above
  // eg: is it common? Do I care about what's in it?
  def exploreSections(
      backupFilePath: String,
      sectionNames: List[String]
  ): Unit = {
    val wiktionary = Wiktionary.extractSections(
      Wiktionary
        .loadWiktionaryDump(backupFilePath),
      sectionNames.toArray
    )
    sectionNames.foreach(sectionName => {
      wiktionary
        .select("text", sectionName)
        .where(col(sectionName) =!= "")
        .limit(500)
        .coalesce(1)
        .write
        .json(sectionName)
    })
  }
}
