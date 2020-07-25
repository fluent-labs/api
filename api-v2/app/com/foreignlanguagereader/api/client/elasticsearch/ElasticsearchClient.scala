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
  ElasticsearchCacheRequest,
  ElasticsearchSearchRequest,
  ElasticsearchSearchResponse
}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchRequest,
  MultiSearchResponse
}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps
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

  // Holds inserts that weren't performed due to the circuit breaker being closed
  // Mutability is a risk but a decent tradeoff because the cost of losing an insert or two is low
  // But setting up actors makes this much more complicated and is not worth it IMO
  private[this] val insertionQueue
    : ConcurrentLinkedQueue[ElasticsearchCacheRequest] =
    new ConcurrentLinkedQueue()

  // Read only way to check state
  // Only use this for testing
  def getQueue: Seq[ElasticsearchCacheRequest] = insertionQueue.asScala.toSeq

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
    requests: Seq[ElasticsearchSearchRequest[T]]
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
                  result =>
                    ElasticsearchSearchResponse.fromResult[T](request, result)
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

        if (toSave.nonEmpty) {
          insertionQueue.addAll(toSave.asJava)

          // Spin up a thread to work on the queue
          // And return to user without waiting for it to finish
          val future = Future.apply(startInserting())
          logger.info(
            s"Saving results back to elasticsearch using future $future: $toSave"
          )
        }

        results.map(_.result)
      })
  }

  // The breaker guard is pretty important, or else we just infinitely retry insertions
  // That will quickly consume the thread pool.
  @scala.annotation.tailrec
  final def startInserting(): Unit = if (breaker.isOpen) {
    // We could check if the queue is empty but that opens us up to race conditions.
    // Removing and null checking is atomic, and the queue is thread safe, so we are protected.
    insertionQueue.poll() match {
      case null => logger.info("Finished inserting") // scalastyle:off
      case item: ElasticsearchCacheRequest =>
        save(item)
        startInserting()
    }
  }

  def save(request: ElasticsearchCacheRequest): Unit = {
    logger.info(s"Saving to elasticsearch: $request")
    val future =
      execute(bulk(request.requests)).map {
        case Success(CircuitBreakerAttempt(_)) =>
          logger
            .info(s"Successfully saved to elasticsearch: $request")
        case Success(CircuitBreakerNonAttempt()) =>
          logger
            .info(
              "Elasticsearch connection is unhealthy, retrying insert when it is healthy."
            )
          insertionQueue.add(request)
        case Failure(e) =>
          logger.warn(
            s"Failed to persist request to elasticsearch: $request, ${e.getMessage}",
            e
          )
          request.retries match {
            case Some(retries) =>
              logger.info(s"Retrying request individually: $request")
              insertionQueue.addAll(retries asJava)
            case None =>
              logger.warn(
                s"Not retrying request because this is our second attempt: $request"
              )
          }
      }

    // We want to iterate through one by one
    // Not set off hundreds of inserts concurrently
    // The circuit breaker will time this out before await does.
    val _ = Await.result(future, timeout + Duration(30, TimeUnit.SECONDS))
  }

  // Common wrappers for all requests below here

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

  // Circuit breaker stuff below here

  breaker.onClose({
    logger.info("Retrying inserts because elasticsearch is healthy again.")
    val _ = Future.apply(startInserting())
  })

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
