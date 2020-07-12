package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.TimeUnit

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http.JavaClient
import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.concurrent.Future
import scala.concurrent.duration.Duration

// Holder to enable easy mocking
@Singleton
class ElasticsearchClientHolder @Inject()(config: Configuration) {
  val elasticSearchUrl: String = config.get[String]("elasticsearch.url")
  val client = ElasticClient(JavaClient(ElasticProperties(elasticSearchUrl)))
  val elasticSearchTimeout =
    Duration(config.get[Int]("elasticsearch.timeout"), TimeUnit.SECONDS)

  implicit val options: CommonRequestOptions = CommonRequestOptions(
    timeout = elasticSearchTimeout,
    masterNodeTimeout = elasticSearchTimeout
  )

  def execute[T, U](request: T)(implicit handler: Handler[T, U],
                                manifest: Manifest[U]): Future[Response[U]] =
    client.execute(request)
}
