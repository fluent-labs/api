package com.foreignlanguagereader.domain.repository

import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.common.CircuitBreakerResult
import com.foreignlanguagereader.domain.client.database.DatabaseClient
import com.foreignlanguagereader.domain.dao.{WordDAO, WordSchema}
import play.api.Logger
import slick.jdbc.H2Profile.api._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WordRepository @Inject() (
    db: DatabaseClient,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  val word = TableQuery[WordSchema]

  def setup(): Unit = {
    logger.info("Running setup")
    val setupQuery = DBIO.seq(
      word.schema.createIfNotExists,
      word += WordDAO(Language.ENGLISH, "test", PartOfSpeech.NOUN, "test")
    )
    val setupFuture: Future[CircuitBreakerResult[Unit]] =
      Nested(
        db.run(setupQuery)
          .map(_ => {
            logger.info("Did setup")
            db.run(word.result)
          })
          .flatten
      ).map(println).value
  }
}
