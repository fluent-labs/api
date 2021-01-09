package com.foreignlanguagereader.domain.client.database

import akka.actor.ActorSystem
import com.foreignlanguagereader.domain.client.common.Circuitbreaker
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import play.api.Configuration

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DatabaseClient @Inject() (
    config: Configuration,
    val system: ActorSystem,
    metrics: MetricsReporter
) {
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("database-context")
  val breaker: Circuitbreaker = new Circuitbreaker(system, ec, "database")

}
