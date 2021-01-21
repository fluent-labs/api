package com.foreignlanguagereader.domain.client.database

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.circuitbreaker.{
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import com.foreignlanguagereader.domain.metrics.label.DatabaseMethod
import com.foreignlanguagereader.domain.metrics.label.DatabaseMethod.DatabaseMethod
import play.api.Logger
import slick.dbio.{DBIOAction, NoStream}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DatabaseClient @Inject() (
    val system: ActorSystem,
    db: DatabaseConnection,
    metrics: MetricsReporter
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("database-context")
  val logger: Logger = Logger(this.getClass)
  val breaker: Circuitbreaker = new Circuitbreaker(system, ec, "database")

  def runQuery[R](query: DBIOAction[R, NoStream, Nothing]) =
    run(query, DatabaseMethod.QUERY)

  def runSetup[R](query: DBIOAction[R, NoStream, Nothing]) =
    run(query, DatabaseMethod.SETUP)

  def run[R](
      query: DBIOAction[R, NoStream, Nothing],
      method: DatabaseMethod
  ): Future[CircuitBreakerResult[R]] = {
    val timer = metrics.reportDatabaseRequestStarted(method)
    breaker.withBreaker(e => {
      metrics.reportDatabaseFailure(timer, method)
      logger.error(s"Failed to execute query $query: ${e.getMessage}", e)
    })({
      val result = db.run[R](query)
      result.onComplete(_ => metrics.reportDatabaseRequestFinished(timer))
      result
    })
  }
}
