package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.ConcurrentLinkedQueue

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
import com.sksamuel.elastic4s.requests.bulk.BulkCompatibleRequest
import com.sksamuel.elastic4s.requests.indexes.IndexRequest
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchRequest,
  MultiSearchResponse
}
import com.sksamuel.elastic4s.requests.update.UpdateRequest
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

@Singleton
class ElasticsearchClient @Inject()(config: Configuration,
                                    val client: ElasticsearchClientHolder,
                                    val system: ActorSystem)
    extends Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)
  implicit val ec: ExecutionContext =
    system.dispatchers.lookup("elasticsearch-context")

  val attemptsIndex = "attempts"
  val maxConcurrentInserts = 5

  // Holds inserts that weren't performed due to the circuit breaker being closed
  // Mutability is a risk but a decent tradeoff because the cost of losing an insert or two is low
  // But setting up actors makes this much more complicated and is not worth it IMO
  private[this] val insertionQueue
    : ConcurrentLinkedQueue[Either[IndexRequest, UpdateRequest]] =
    new ConcurrentLinkedQueue()

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
  def findFromCacheOrRefetch[T: Indexable](
    requests: Seq[ElasticsearchRequest[T]]
  )(implicit hitReader: HitReader[T],
    attemptsHitReader: HitReader[LookupAttempt],
    tag: ClassTag[T]): Future[Seq[Option[Seq[T]]]] = {
    // Fork and join for getting each request
    Future
      .sequence(
        requests
          .map(
            request =>
              // Checks elasticsearch first. Responses will have ES results or none
              executeWithOption[MultiSearchRequest, MultiSearchResponse](
                request.query
              ).map(
                  result => ElasticsearchResponse.fromResult[T](request, result)
                )
                // This is where fetchers are called to get results if they aren't in elasticsearch
                // There is also logic to remember what has been fetched from external sources
                // So that we don't try too many times on the same query
                .map(_.getResultOrFetchFromSource)
                .flatten
          )
      )
      // Block here until we have finished with all of the requests
      .map(results => {
        // Asychronously save results back to elasticsearch
        // toIndex only exists when results were fetched from the fetchers
        val toSave = results.flatMap(_.toIndex).flatten
        logger.info(s"Saving results back to elasticsearch: $toSave")
        Future.apply(saveToCacheInBatches(toSave))

        results.map(_.result)
      })
  }

  // Saves documents in batches, and retries each item in the batch in case of a batch failure.
  // Why? If there is one bad document, it'll fail the batch, but the rest of the requests may be fine.
  private[this] def saveToCacheInBatches(
    requests: Seq[Either[IndexRequest, UpdateRequest]]
  ): Unit = {
    requests
      .grouped(maxConcurrentInserts)
      .foreach(batch => {
        val queries: Seq[BulkCompatibleRequest] = batch.map {
          case Left(index)   => index
          case Right(update) => update
        }
        execute(bulk(queries)) map {
          case Success(CircuitBreakerAttempt(s)) =>
            logger
              .info(s"Successfully saved to elasticsearch: ${s.items}")
            batch.foreach(request => saveToCache(request))
          case Success(CircuitBreakerNonAttempt()) =>
            logger
              .info(
                s"Failed to persist to elasticsearch because the circuit breaker is closed"
              )
            insertionQueue.addAll(batch asJava)
          case Failure(e) =>
            logger.warn(
              s"Failed to persist to elasticsearch in bulk, retrying individually: $requests",
              e
            )
            batch.foreach(request => saveToCache(request))
        }
      })
  }

  // Useful for retrying batches to see if there are good requests in the batch
  private[this] def saveToCache(
    request: Either[IndexRequest, UpdateRequest]
  ): Unit =
    (request match {
      // This works around a limitation in the client library
      // Since there is no parent type for the different requests
      case Left(index)   => execute(index)
      case Right(update) => execute(update)
    }).map {
      case Success(CircuitBreakerAttempt(s)) =>
        logger
          .info(s"Successfully saved to elasticsearch: $s")
      case Success(CircuitBreakerNonAttempt()) =>
        logger
          .info(
            s"Failed to persist to elasticsearch because the circuit breaker is closed"
          )
        insertionQueue.add(request)
      case Failure(e) =>
        logger.warn(s"Failed to persist to elasticsearch: $request", e)
    }

  breaker.onClose(() => {
    logger.info("Retrying inserts because the circuit breaker is closed.")
    retryInserts()
  })

  // The breaker guard is pretty important, or else we just infinitely retry insertions
  // That will quickly consume the thread pool.
  @scala.annotation.tailrec
  private[this] def retryInserts(): Unit = if (breaker.isOpen) {
    // We could check if the queue is empty but that opens us up to race conditions.
    // Removing and null checking is atomic, and the queue is thread safe, so we are protected.
    insertionQueue.poll() match {
      case null => logger.info("Finished retrying inserts.") // scalastyle:off
      case item => saveToCache(item)
    }
    retryInserts()
  }

  // Wraps all elasticsearch requests with error handling
  private[this] def execute[T, U](request: T)(
    implicit handler: Handler[T, U],
    manifest: Manifest[U]
  ): Future[Try[CircuitBreakerResult[U]]] =
    withBreaker(client.execute(request) map {
      case RequestSuccess(_, _, _, result) => result
      case RequestFailure(_, _, _, error) =>
        logger.error(
          s"Error executing elasticearch query: $request due to error: ${error.reason}",
          error.asException
        )
        throw error.asException
    })

  // Wrap the result with an option
  private[this] def executeWithOption[T, U](request: T)(
    implicit handler: Handler[T, U],
    manifest: Manifest[U]
  ): Future[CircuitBreakerResult[Option[U]]] = execute(request).map {
    case Success(CircuitBreakerAttempt(value)) =>
      CircuitBreakerAttempt(Some(value))
    case Success(CircuitBreakerNonAttempt()) =>
      CircuitBreakerNonAttempt()
    case Failure(e) =>
      logger.error(
        s"Error executing elasticearch query: $request due to error: ${e.getMessage}",
        e
      )
      CircuitBreakerAttempt(None)
  }

  /**
    * This makes the circuitbreaker know when elasticsearch requests are failing
    * Without this, errors show up as successes.
    * @param result The result to work with
    * @tparam T Whatever the request is. Doesn't matter for this one
    * @return Was it a failure?
    */
  override def defaultIsSuccess[T](result: T): Boolean = result match {
    case RequestSuccess(_, _, _, _) => true
    case RequestFailure(_, _, _, _) => false
    case _                          => true
  }
}
