package com.foreignlanguagereader.jobs.definitions

import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.jobs.SparkSessionBuilder
import org.apache.spark.sql.{Dataset, SparkSession}
import org.elasticsearch.spark.sql._

import scala.reflect.runtime.universe.TypeTag

// Typetag needed to tell spark how to encode as a dataset
abstract class DefinitionsParsingJob[T <: DefinitionEntry: TypeTag](
    s3BasePath: String,
    source: DefinitionSource
) {
  def main(args: Array[String]): Unit = {
    val sourceName = source.toString.replace("_", "-").toLowerCase
    val backupFileName = sys.env("backup_file_name")
    val path = s"$s3BasePath/$backupFileName"

    implicit val spark: SparkSession = SparkSessionBuilder
      .build(s"${sourceName.replace("-", " ")} parse")
    import spark.implicits._

    val data = loadFromPath(path)
    val cacheable = data.map(entry => DefinitionEntry.toCacheable[T](entry))
    cacheable.saveToEs(s"definitions-$sourceName")
  }

  def loadFromPath(path: String)(implicit spark: SparkSession): Dataset[T]
}
