package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.database.DatabaseClient
import play.api.Logger
import slick.dbio.{DBIOAction, NoStream}
import slick.lifted.TableQuery
import slick.model.Table

import scala.concurrent.ExecutionContext

abstract class Repository[T](
    name: String,
    db: DatabaseClient,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)
  val query = TableQuery[T]

  def makeSetupQuery(query: TableQuery[T]): DBIOAction[Unit, NoStream, _]

  def setup(): Unit = {
    logger.info(s"Running setup for repository $name")
    db.run(makeSetupQuery(query)).foreach {
      case CircuitBreakerAttempt(_) =>
        logger.info(s"Successfully set up repository $name")
      case CircuitBreakerFailedAttempt(e) =>
        logger.error(s"Failed to set up repository $name: ${e.getMessage}", e)
      case CircuitBreakerNonAttempt() =>
        logger.error(
          s"Failed to set up repository $name: circuit breaker was closed"
        )
      // TODO consider some attempt at retry
    }
  }
}
