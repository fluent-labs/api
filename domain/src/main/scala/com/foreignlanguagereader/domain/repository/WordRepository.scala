package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.database.DatabaseClient
import com.foreignlanguagereader.domain.dao.{WordDAO, WordSchema}
import play.api.Logger
import slick.jdbc.H2Profile.api._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WordRepository @Inject() (
    db: DatabaseClient,
    override implicit val ec: ExecutionContext
) extends Repository[WordSchema]("word", db, ec) {
  override val logger: Logger = Logger(this.getClass)

  override def makeSetupQuery(
      word: TableQuery[WordSchema]
  ): DBIOAction[Unit, NoStream, _] =
    DBIO.seq(
      word.schema.createIfNotExists,
      word += WordDAO(Language.ENGLISH, "test", PartOfSpeech.NOUN, "test")
    )
}
