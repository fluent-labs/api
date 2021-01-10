package com.foreignlanguagereader.domain.client.database

import akka.actor.ActorSystem
import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.basic.DatabaseConfig
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcProfile

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DatabaseConnection @Inject() (
    val system: ActorSystem
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("database-context")
  val dc = DatabaseConfig.forConfig[JdbcProfile]("database")
  val database = dc.db

  def run[R](query: DBIOAction[R, NoStream, Nothing]): Future[R] =
    database.run[R](query)
}
