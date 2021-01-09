package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.client.database.DatabaseConnectionHolder
import com.foreignlanguagereader.domain.dao.WordDAO
import slick.jdbc.H2Profile.api._

import javax.inject.Inject
import scala.concurrent.Future

class WordRepository @Inject() (db: DatabaseConnectionHolder) {
  val word = TableQuery[WordDAO]
  val setup = DBIO.seq(
    word.schema.createIfNotExists,
    word += (Language.ENGLISH, "test", PartOfSpeech.NOUN, "test", None, None, false, None, "test")
  )

  val setupFuture: Future[Unit] = db.run(setup)
}
