package com.foreignlanguagereader.domain.repository

import akka.actor.ActorSystem
import com.google.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

class DatabaseConnectionHolder @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val system: ActorSystem
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("database-context")
  val db = dbConfigProvider.get
}
