package com.foreignlanguagereader.api.client.elasticsearch

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http.JavaClient
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.Future

// Holder to enable easy mocking
class ElasticsearchClientHolder @Inject()(config: Configuration) {
  val elasticSearchUrl: String = config.get[String]("elasticsearch.url")
  val client = ElasticClient(JavaClient(ElasticProperties(elasticSearchUrl)))

  def execute[T, U](request: T)(implicit handler: Handler[T, U],
                                manifest: Manifest[U]): Future[Response[U]] =
    client.execute(request)
}
