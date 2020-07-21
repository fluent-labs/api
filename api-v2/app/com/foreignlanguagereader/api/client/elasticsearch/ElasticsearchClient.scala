package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.{ConcurrentLinkedQueue, TimeUnit}

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

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
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
  def getFromCache[T: Indexable](requests: Seq[ElasticsearchRequest[T]])(
    implicit hitReader: HitReader[T],
    tag: ClassTag[T]
  ): Future[Seq[Option[Seq[T]]]] = {
    // Checks elasticsearch first. Responses will have ES results or none
    Future
      .sequence(requests.map(r => getMultiple(r.query)))
      .map(
        results =>
          requests.zip(results) map {
            case (request, result) =>
              ElasticsearchResponse.fromResult(request, result)
        }
      )

      // This is where fetchers are called to get results if they aren't in elasticsearch
      // There is also logic to remember what has been fetched from external sources
      // So that we don't try too many times on the same query
      .map(r => Future.sequence(r.map(_.getResultOrFetchFromSource)))
      // Now we have a future of a future
      .flatten
      .map(results => {
        // Asychronously save results back to elasticsearch
        // toIndex only exists when results were fetched from the fetchers
        Future.apply(saveToCacheInBatches(results.flatMap(_.toIndex).flatten))

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
        withBreaker(client.execute {
          bulk(queries)
        }) map {
          case Success(CircuitBreakerAttempt(s)) =>
            logger
              .info(s"Successfully saved to elasticsearch: ${s.body}")
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
      case Left(index)   => withBreaker(client.execute(index))
      case Right(update) => withBreaker(client.execute(update))
    }).map {
      case Success(CircuitBreakerAttempt(s)) =>
        logger
          .info(s"Successfully saved to elasticsearch: ${s.body}")
      case Success(CircuitBreakerNonAttempt()) =>
        logger
          .info(
            s"Failed to persist to elasticsearch because the circuit breaker is closed"
          )
        insertionQueue.add(request)
      case Failure(e) =>
        logger.warn(s"Failed to persist to elasticsearch: $request", e)
    }

  breaker.onOpen(() => {
    logger.info("Retrying inserts because the circuit breaker is open.")
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
        }
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
