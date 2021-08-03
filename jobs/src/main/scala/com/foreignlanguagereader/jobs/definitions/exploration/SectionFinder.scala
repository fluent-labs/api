package com.foreignlanguagereader.jobs.definitions.exploration

import com.foreignlanguagereader.jobs.definitions.Wiktionary
import org.apache.log4j.{LogManager, Logger}
import org.apache.spark.sql.SparkSession

import scala.util.{Failure, Success, Try}

// Use this when you want to know what kind of sections a backup has. Good for getting the rough structure of the dump
object SectionFinder {
  val jobName = "Wiktionary Section Extractor"
  val backupsBasePath =
    "s3a://foreign-language-reader-content/definitions/wiktionary"

  val backups = Map(
    "chinese" -> "zhwiktionary-20210201-pages-meta-current.xml",
    "danish" -> "dawiktionary-20210201-pages-meta-current.xml",
    "english" -> "enwiktionary-20210201-pages-meta-current.xml",
    "simple_english" -> "simplewiktionary-20200301-pages-meta-current.xml",
    "spanish" -> "eswiktionary-20210201-pages-meta-current.xml"
  )

  @transient lazy val log: Logger =
    LogManager.getLogger(jobName)

  def main(args: Array[String]): Unit = {
    implicit val spark: SparkSession = SparkSession.builder
      .appName(jobName)
      .getOrCreate()

    val errorCount = backups.map { case (dictionary, path) =>
      findSectionsFromBackup(
        s"$backupsBasePath/$path",
        s"$backupsBasePath/sections/$dictionary.csv"
      )
    }.sum

    if (errorCount > 0) {
      throw new IllegalStateException(s"Failed to extract $errorCount backups.")
    }
  }

  def findSectionsFromBackup(path: String, resultPath: String)(implicit
      spark: SparkSession
  ): Int = {
    log.info(s"Extracting from $path to $resultPath")

    log.info(s"Extracting wiktionary dump from $path")
    Try(Wiktionary.loadWiktionaryDump(path)).map(wiktionaryRaw => {
      log.info(s"Successfully extracted dump")
      Wiktionary
        .getHeadings(wiktionaryRaw, 1)
        .write
        .csv(resultPath)
    }) match {
      case _: Success[Unit] =>
        log.info("Successfully found sections")
        0
      case Failure(e) =>
        log.error("Failed to extract sections", e)
        1
    }
  }
}
