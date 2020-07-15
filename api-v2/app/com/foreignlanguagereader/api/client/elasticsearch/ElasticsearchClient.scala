package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.{
  ElasticsearchRequest,
  ElasticsearchResponse
}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.requests.indexes.IndexRequest
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchRequest,
  MultiSearchResponse
}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

@Singleton
class ElasticsearchClient @Inject()(config: Configuration,
                                    val client: ElasticsearchClientHolder,
                                    val system: ActorSystem)
    extends Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("elasticsearch-context")
  override val timeout =
    Duration(config.get[Int]("elasticsearch.timeout"), TimeUnit.SECONDS)

  val attemptsIndex = "attempts"
  val maxConcurrentInserts = 5

  /**
    *
    * We cache in elasticsearch because some content sources have request rate limits.
    * This caching prevents us from using requests for things we have already searched for,
    * and puts a limit on the number of times we will retry a search that hasn't given us results before.
    *
    * @param requests The search requests to cache.
    * @param hitReader Automatically generated from Reads[T]
    * @param tag The class of T, captured at runtime. This is needed to make a Seq of an arbitrary type due to JVM type erasure.
    * @tparam T A case class with Reads[T] and Writes[T]
    * @return
    */
  def getFromCache[T: Indexable](requests: Seq[ElasticsearchRequest[T]])(
    implicit hitReader: HitReader[T],
    tag: ClassTag[T]
  ): Future[Seq[Option[Seq[T]]]] = {
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
      .map(r => Future.sequence(r.map(_.getResultOrFetchFromSource)))
      // Now we have a future of a future
      .flatten
      .map(results => {
        // Asychronously save results back to elasticsearch
        // toIndex only exists when results were fetched from the fetchers
        Future.apply(saveToCache(results.flatMap(_.toIndex).flatten))

        results.map(_.result)
      })
  }

  // Saves back to elasticsearch in baches
  private[this] def saveToCache(requests: Seq[IndexRequest]): Unit = {
    requests
      .grouped(maxConcurrentInserts)
      .foreach(
        batch =>
          withBreaker(client.execute {
            bulk(batch)
          }, _ => true) map {
            case Success(CircuitBreakerAttempt(s)) =>
              logger
                .info(s"Successfully saved to elasticsearch: ${s.body}")
            case Success(CircuitBreakerNonAttempt()) =>
              logger
                .info(
                  s"Failed to persist to elasticsearch because the circuit breaker is closed"
                )
            case Failure(e) =>
              logger.warn(s"Failed to persist to elasticsearch: $requests", e)
        }
      )
  }

  // Get method for multisearch, but with error handling
  private[this] def getMultiple[T](
    request: MultiSearchRequest
  ): Future[CircuitBreakerResult[Option[MultiSearchResponse]]] = {
    withBreaker(
      client
        .execute[MultiSearchRequest, MultiSearchResponse](request)
        .map {
          case RequestSuccess(_, _, _, result) => result
          case RequestFailure(_, _, _, error) =>
            logger.error(
              s"Error fetching from elasticsearch for request: $request due to error: ${error.reason}",
              error.asException
            )
            throw error.asException
        },
      _ => true
    ).map {
      case Success(CircuitBreakerAttempt(value)) =>
        CircuitBreakerAttempt(Some(value))
      case Success(CircuitBreakerNonAttempt()) =>
        CircuitBreakerNonAttempt[Option[MultiSearchResponse]]()
      case Failure(e) =>
        logger.error(
          s"Error fetching from elasticsearch for request: $request due to error: ${e.getMessage}",
          e
        )
        CircuitBreakerAttempt(None)
    }
  }
}
