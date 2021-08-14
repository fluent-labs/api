package com.foreignlanguagereader.jobs.definitions

import io.fluentlabs.content.types.external.definition.DefinitionEntry
import io.fluentlabs.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.jobs.SparkSessionBuilder
import org.apache.log4j.{LogManager, Logger}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.elasticsearch.spark.sql._

import scala.reflect.runtime.universe.TypeTag

// Typetag needed to tell spark how to encode as a dataset
abstract class DefinitionsParsingJob[T <: DefinitionEntry: TypeTag](
    s3BasePath: String,
    defaultBackupFileName: String,
    source: DefinitionSource
) {
  @transient lazy val log: Logger =
    LogManager.getLogger("Definitions parsing job")

  def main(args: Array[String]): Unit = {
    val sourceName = source.toString.replace("_", "-").toLowerCase
    val backupFileName =
      sys.env.getOrElse("backup_file_name", defaultBackupFileName)
    val path = s"$s3BasePath/$backupFileName"
    log.info(s"Getting file from $path")

    implicit val spark: SparkSession = SparkSessionBuilder
      .build(s"${sourceName.replace("-", " ")} parse")
    import spark.implicits._
    log.info("Created spark session")

    log.info("Loading data")
    val data = loadFromPath(path)
    log.info("Loaded data")

    log.info("Serializing data")
    val cacheable = data.map(entry => DefinitionEntry.toCacheable[T](entry))
    log.info("Serialized data")

    val index = s"definitions-$sourceName"
    log.info(s"Saving to elasticsearch index $index")
    cacheable.saveToEs(index)
    log.info("Finished saving to elasticsearch")
  }

  def loadFromPath(path: String)(implicit spark: SparkSession): Dataset[T]
}
