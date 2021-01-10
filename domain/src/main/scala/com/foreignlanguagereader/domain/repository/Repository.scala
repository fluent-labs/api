package com.foreignlanguagereader.domain.repository

import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt
}
import com.foreignlanguagereader.domain.client.database.DatabaseClient
import play.api.Logger
import slick.dbio.{DBIOAction, NoStream}

import scala.concurrent.ExecutionContext

abstract class Repository(
    name: String,
    db: DatabaseClient,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  def makeSetupQuery(): DBIOAction[Unit, NoStream, _]

  def setup(): Unit = {
    logger.info(s"Running setup for repository $name")
    db.run(makeSetupQuery()).foreach {
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
