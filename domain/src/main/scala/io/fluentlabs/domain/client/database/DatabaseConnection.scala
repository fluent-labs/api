package io.fluentlabs.domain.client.database

import akka.actor.ActorSystem
import com.google.inject.Inject
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
  val database = DatabaseConnection.dc.db

  def run[R](query: DBIOAction[R, NoStream, Nothing]): Future[R] =
    database.run[R](query)
}

object DatabaseConnection {
  val dc = DatabaseConfig.forConfig[JdbcProfile]("database")
}
