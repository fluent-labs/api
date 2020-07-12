package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.{
  ElasticsearchRequest,
  ElasticsearchResponse
}
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus.ReadinessStatus
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.requests.indexes.IndexRequest
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchRequest,
  MultiSearchResponse
}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

@Singleton
class ElasticsearchClient @Inject()(config: Configuration,
                                    val client: ElasticsearchClientHolder,
                                    val system: ActorSystem) {
  val logger: Logger = Logger(this.getClass)

  // What's with all the awaits? elastic4s does not implement connection timeouts.
  // Instead, we need to implement them ourselves.
  // Not the end of the world, we would have been blocking here anyway since it is IO
  // We use this thread pool to keep it out of the main server threads
  implicit val myExecutionContext: ExecutionContext =
    system.dispatchers.lookup("elasticsearch-context")

  val elasticSearchTimeout =
    Duration(config.get[Int]("elasticsearch.timeout"), TimeUnit.SECONDS)

  val attemptsIndex = "attempts"
  val maxConcurrentInserts = 5
  val maxFetchRetries = 5

  def checkConnection(timeout: Duration): ReadinessStatus = {
    Try(Await.result(client.execute(indexExists(attemptsIndex)), timeout)) match {
      case Success(result) =>
        result match {
          case RequestSuccess(_, _, _, result) if result.exists =>
            ReadinessStatus.UP
          case RequestSuccess(_, _, _, _) =>
            logger.error(
              s"Error connecting to elasticsearch: index $attemptsIndex does not exist"
            )
            ReadinessStatus.DOWN
          case RequestFailure(_, _, _, error) =>
            logger.error(
              s"Error connecting to elasticsearch: ${error.reason}",
              error.asException
            )
            ReadinessStatus.DOWN
        }
      case Failure(e) =>
        logger
          .error(s"Failed to connect to elasticsearch: ${e.getMessage}", e)
        ReadinessStatus.DOWN
    }
  }

  // Why Type tag? Runtime information needed to make a seq of an arbitrary type.
  // JVM type erasure would remove this unless we pass it along this way
  def getFromCache[T: Indexable](requests: Seq[ElasticsearchRequest[T]])(
    implicit hitReader: HitReader[T],
    tag: ClassTag[T]
  ): Future[Option[Seq[T]]] = {
    // Checks elasticsearch first. Responses will have ES results or none
    val responses: Future[Seq[ElasticsearchResponse[T]]] = Future
      .sequence(requests.map(r => getMultiple(r.query)))
      .map(
        results =>
          requests.zip(results) map {
            case (request, result) =>
              ElasticsearchResponse.fromResult(request, result)
        }
      )

    responses
    // This is where fetchers are called to get results if they aren't in elasticsearch
    // There is also logic to remember what has been fetched from external sources
    // So that we don't try too many times on the same query
      .map(r => Future.sequence(r.map(_.getResult)))
      .flatten
      .map(results => {
        // Asychronously save results back to elasticsearch
        // toIndex only exists when results were fetched from the fetchers
        Future.apply(saveToCache(results.flatMap(_.toIndex).flatten))

        val r = results.flatMap(_.result).flatten
        if (r.isEmpty) None else Some(r)
      })
  }

  // Saves back to elasticsearch in baches
  private[this] def saveToCache(requests: Seq[IndexRequest]): Unit = {
    requests
      .grouped(maxConcurrentInserts)
      .foreach(
        batch =>
          Try(client.execute {
            bulk(batch)
          }) match {
            case Success(s) =>
              s.foreach(
                r =>
                  logger.info(s"Successfully saved to elasticsearch: ${r.body}")
              )
            case Failure(e) =>
              logger.warn(s"Failed to persist to elasticsearch: $requests", e)
        }
      )
  }

  // Get method for multisearch, but with error handling
  private[this] def getMultiple[T](
    request: MultiSearchRequest
  ): Future[Option[MultiSearchResponse]] = {
    client
      .execute[MultiSearchRequest, MultiSearchResponse](request)
      .map {
        case RequestSuccess(_, _, _, result) =>
          Some(result)
        case RequestFailure(_, _, _, error) =>
          logger.error(
            s"Error fetching from elasticsearch for request: $request due to error: ${error.reason}",
            error.asException
          )
          None
      }
      .recover {
        case e: Exception =>
          logger.error(
            s"Failed to fetch from elasticsearch for request: $request",
            e
          )
          None
      }
  }
}
