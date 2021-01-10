package com.foreignlanguagereader.domain.client.database

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.common.Circuitbreaker
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import play.api.{Configuration, Logger}
import slick.dbio.{DBIOAction, NoStream}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DatabaseClient @Inject() (
    val system: ActorSystem,
    config: Configuration,
    db: DatabaseConnection,
    metrics: MetricsReporter
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("database-context")
  val logger: Logger = Logger(this.getClass)
  val breaker: Circuitbreaker = new Circuitbreaker(system, ec, "database")

  def run[R](query: DBIOAction[R, NoStream, Nothing]) = {
    breaker.withBreaker(e => {
      logger.error(s"Failed to execute query $query: ${e.getMessage}", e)
    })(db.run[R](query))
  }
}
