package com.foreignlanguagereader.api.client.elasticsearch

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

import scala.concurrent.{ExecutionContext, Future}
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
          client.addInsertsToQueue(toSave)

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
  def startInserting(): Future[Unit] =
    if (breaker.isOpen) {
      // We could check if the queue is empty but that opens us up to race conditions.
      // Removing and checking is atomic, and the queue is thread safe, so we are protected.
      client.nextInsert() match {
        case Some(item) =>
          save(item).map(_ => startInserting()).flatten
        case None =>
          logger.info("Finished inserting")
          Future.apply(())
      }
    } else { Future.apply(()) }

  def save(request: ElasticsearchCacheRequest): Future[Unit] = {
    logger.info(s"Saving to elasticsearch: $request")
    execute(bulk(request.requests)).map {
      case Success(CircuitBreakerAttempt(_)) =>
        logger
          .info(s"Successfully saved to elasticsearch: $request")
      case Success(CircuitBreakerNonAttempt()) =>
        logger
          .info(
            "Elasticsearch connection is unhealthy, retrying insert when it is healthy."
          )
        client.addInsertToQueue(request)
      case Failure(e) =>
        logger.warn(
          s"Failed to persist request to elasticsearch: $request, ${e.getMessage}",
          e
        )
        request.retries match {
          case Some(retries) =>
            logger.info(s"Retrying request individually: $request")
            client.addInsertsToQueue(retries)
          case None =>
            logger.warn(
              s"Not retrying request because this is our second attempt: $request"
            )
        }
    }
  }

  // Common wrappers for all requests below here

  // Unwraps all elasticsearch requests so there can be a single failure path
  private[this] def execute[T, U](request: T)(
    implicit handler: Handler[T, U],
    manifest: Manifest[U]
  ): Future[Try[CircuitBreakerResult[U]]] =
    withBreaker(client.execute(request) map {
      case RequestSuccess(_, _, _, result) => result
      case RequestFailure(_, _, _, error)  => throw error.asException
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
