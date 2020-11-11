package com.foreignlanguagereader.domain.client.elasticsearch

import akka.Done
import akka.actor.CoordinatedShutdown
import javax.inject.Inject
import org.apache.http.HttpHost
import org.elasticsearch.client.{
  RequestOptions,
  RestClient,
  RestHighLevelClient
}
import org.elasticsearch.client.indices.CreateIndexRequest
import org.testcontainers.elasticsearch.ElasticsearchContainer
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

/**
  * Class to automatically configure an elasticsearch client. That's it.
  *
  * @param config
  * @param cs
  * @param ec
  */
class ElasticsearchClientConfig @Inject() (
    config: Configuration,
    cs: CoordinatedShutdown,
    implicit val ec: ExecutionContext
) {
  val isLocal: Boolean = config.get[Boolean]("local")
  val scheme: String = config.get[String]("elasticsearch.scheme")
  val url: String = config.get[String]("elasticsearch.url")
  val port: Int = config.get[Int]("elasticsearch.port")

  val httpHost: HttpHost = if (isLocal) {
    createLocalElasticsearch()
  } else {
    new HttpHost(url, port, scheme)
  }

  val javaClient = new RestHighLevelClient(
    RestClient.builder(httpHost)
  )

  if (isLocal) {
    createElasticsearchIndexes(javaClient, List("attempts", "definitions"))
  }

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

  def createElasticsearchIndexes(
      client: RestHighLevelClient,
      indexes: List[String]
  ): Unit = {
    indexes
      .map(index => new CreateIndexRequest(index))
      .foreach(request =>
        client.indices().create(request, RequestOptions.DEFAULT)
      )
  }
}
