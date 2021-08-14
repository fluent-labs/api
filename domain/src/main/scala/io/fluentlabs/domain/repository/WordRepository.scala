package io.fluentlabs.domain.repository

import io.fluentlabs.content.types.Language
import io.fluentlabs.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.dao.WordSchema
import io.fluentlabs.domain.client.circuitbreaker.CircuitBreakerResult
import io.fluentlabs.domain.client.database.DatabaseClient
import io.fluentlabs.domain.dao.{WordDAO, WordSchema}
import play.api.Logger
import slick.jdbc.H2Profile.api._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WordRepository @Inject() (
    db: DatabaseClient,
    override implicit val ec: ExecutionContext
) extends Repository("word", db, ec) {
  override val logger: Logger = Logger(this.getClass)

  val words = TableQuery[WordSchema]

  def getAllWords: Future[CircuitBreakerResult[Seq[WordDAO]]] = {
    db.runQuery(words.result)
  }

  override def makeSetupQuery(): DBIOAction[Unit, NoStream, _] =
    DBIO.seq(
      words.schema.createIfNotExists,
      words += WordDAO(Language.ENGLISH, "test", PartOfSpeech.NOUN, "test")
    )
}
