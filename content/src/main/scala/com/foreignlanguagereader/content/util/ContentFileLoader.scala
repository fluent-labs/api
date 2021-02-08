package com.foreignlanguagereader.content.util

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}

import java.io.{BufferedReader, InputStreamReader}
import java.util.stream.Collectors
import scala.util.{Failure, Success, Try}

object ContentFileLoader {
  val logger: Logger = Logger(this.getClass)

  def loadResourceFile(path: String): String = {
    Try {
      val file = this.getClass
        .getResourceAsStream(path)
      val parsed = new BufferedReader(new InputStreamReader(file))
        .lines()
        .collect(Collectors.joining("\n"))
      file.close()
      parsed
    } match {
      case Success(result) => result
      case Failure(exception) =>
        logger.info(
          s"Failed to load content in $path: ${exception.getMessage}",
          exception
        )
        throw exception
    }
  }

  /**
    * Loads a json file from the path, or fails trying.
    *
    * Use this if you want to stop the server from coming up if you can't load this file
    *
    * @param path The location on the classpath of the file. Eg: "/resources/definition/chinese/pronunciation.json"
    * @param rds You don't need to pass this, just make sure your case class has a Reads[T] implicit on it.
    * @tparam T A case class this should be read to.
    * @return
    */
  def loadJsonResourceFile[T](path: String)(implicit rds: Reads[T]): T = {
    Json.parse(loadResourceFile(path)).validate[T] match {
      case JsSuccess(content, _) =>
        logger.info(s"Successfully loaded content from $path")
        content
      case JsError(errors) =>
        val error = s"Failed to parse content in $path: $errors"
        logger.info(error)
        throw new IllegalStateException(error)
    }
  }
}
