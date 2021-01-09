package com.foreignlanguagereader.domain.client.database

import akka.actor.ActorSystem
import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcProfile

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

@Singleton
class DatabaseConnectionHolder @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val system: ActorSystem
) extends HasDatabaseConfigProvider[JdbcProfile] {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("database-context")
  val database = db

  def run[R](query: DBIOAction[R, NoStream, Nothing]) = db.run[R](query)
}
