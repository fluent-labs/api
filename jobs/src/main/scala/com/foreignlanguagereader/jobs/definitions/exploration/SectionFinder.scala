package com.foreignlanguagereader.jobs.definitions.exploration

import com.foreignlanguagereader.jobs.definitions.Wiktionary
import org.apache.log4j.{LogManager, Logger}
import org.apache.spark.sql.SparkSession

// Use this when you want to know what kind of sections a backup has. Good for getting the rough structure of the dump
object SectionFinder {
  val jobName = "Wiktionary Section Extractor"
  val backupsBasePath =
    "s3a://foreign-language-reader-content/definitions/wiktionary/"

  val backups = Map(
//    "simple" -> "simplewiktionary-20200301-pages-meta-current.xml",
    "danish" -> "dawiktionary-20210201-pages-meta-current.xml",
    "spanish" -> "eswiktionary-20210201-pages-meta-current.xml"
  )

  @transient lazy val log: Logger =
    LogManager.getLogger(jobName)

  def main(args: Array[String]): Unit = {
    implicit val spark: SparkSession = SparkSession.builder
      .appName(jobName)
      .getOrCreate()

    backups.foreach {
      case (dictionary, path) =>
        findSectionsFromBackup(
          s"$backupsBasePath/$path",
          s"$backupsBasePath/sections/$dictionary"
        )
    }
  }

  def findSectionsFromBackup(path: String, resultPath: String)(implicit
      spark: SparkSession
  ): Unit = {
    val wiktionaryRaw = Wiktionary
      .loadWiktionaryDump(path)
    Wiktionary
      .getHeadings(wiktionaryRaw, 1)
      .write
      .csv(resultPath)
  }
}
