package com.foreignlanguagereader.domain.client.elasticsearch

import akka.Done
import akka.actor.CoordinatedShutdown
import javax.inject
import javax.inject.Inject
import javax.net.ssl.SSLContext
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.ssl.SSLContexts
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.testcontainers.elasticsearch.ElasticsearchContainer
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

// $COVERAGE-OFF$
/**
  * Class to automatically configure an elasticsearch client. That's it.
  *
  * @param config Config properties
  * @param cs Shutdown hook for akka, to shut down the server when this server quits
  * @param ec Where to run futures.
  */
@inject.Singleton
class ElasticsearchClientConfig @Inject() (
    config: Configuration,
    cs: CoordinatedShutdown,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  val isLocal: Boolean = config.get[Boolean]("local")
  val scheme: String = config.get[String]("elasticsearch.scheme")
  val url: String = config.get[String]("elasticsearch.url")
  val port: Int = config.get[Int]("elasticsearch.port")
  val username: String = config.get[String]("elasticsearch.username")
  val password: String = config.get[String]("elasticsearch.password")
  val truststorePassword: String =
    config.get[String]("elasticsearch.truststore")

  val httpHost: HttpHost = if (isLocal) {
    createLocalElasticsearch()
  } else {
    new HttpHost(url, port, scheme)
  }

  val credentialsProvider: BasicCredentialsProvider = {
    val provider = new BasicCredentialsProvider
    provider.setCredentials(
      AuthScope.ANY,
      new UsernamePasswordCredentials(username, password)
    )
    provider
  }

  val sslContext: SSLContext = {
    val keystorePath = os.root / "etc" / "estruststore" / "api_keystore.jks"
    if (os.exists(keystorePath)) {
      logger.info("Using custom trust store")
      SSLContexts
        .custom()
        .loadTrustMaterial(
          keystorePath.toIO,
          truststorePassword.toCharArray,
          new TrustSelfSignedStrategy()
        )
        .build()
    } else {
      logger.info("Using default trust store")
      SSLContexts.createDefault()
    }
  }

  def getHost: HttpHost = httpHost
  def getClient: RestHighLevelClient =
    new RestHighLevelClient(
      RestClient
        .builder(httpHost)
        .setHttpClientConfigCallback(new HttpClientConfigCallback() {
          override def customizeHttpClient(
              httpClientBuilder: HttpAsyncClientBuilder
          ): HttpAsyncClientBuilder = {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            httpClientBuilder.setSSLContext(sslContext)
          }
        })
    )

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
