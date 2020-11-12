package com.foreignlanguagereader.domain.client.elasticsearch

import akka.Done
import akka.actor.CoordinatedShutdown
import com.google.inject.Provider
import javax.inject.Inject
import org.apache.http.HttpHost
import org.testcontainers.elasticsearch.ElasticsearchContainer
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

// $COVERAGE-OFF$
/**
  * Class to automatically configure an elasticsearch client. That's it.
  *
  * @param config Config properties
  * @param cs Shutdown hook for akka, to shut down the server when this server quits
  * @param ec Where to run futures.
  */
class ElasticsearchClientConfig @Inject() (
    config: Configuration,
    cs: CoordinatedShutdown,
    implicit val ec: ExecutionContext
) extends Provider[HttpHost] {
  val isLocal: Boolean = config.get[Boolean]("local")
  val scheme: String = config.get[String]("elasticsearch.scheme")
  val url: String = config.get[String]("elasticsearch.url")
  val port: Int = config.get[Int]("elasticsearch.port")

  val httpHost: HttpHost = if (isLocal) {
    createLocalElasticsearch()
  } else {
    new HttpHost(url, port, scheme)
  }

  override def get(): HttpHost = httpHost

  def createLocalElasticsearch(): HttpHost = {
    // Launches a dockerized elasticsearch process
    val container = new ElasticsearchContainer(
      "docker.elastic.co/elasticsearch/elasticsearch:7.9.3"
    )
    container.start()

    // Shut down with play
    cs.addTask(CoordinatedShutdown.PhaseServiceUnbind, "shutdown elastic") {
      () =>
        Future {
          container.stop()
          Done
        }
    }

    // Gives connection details - they are randomized to prevent conflicts
    HttpHost.create(container.getHttpHostAddress)
  }
}
// $COVERAGE-ON$
