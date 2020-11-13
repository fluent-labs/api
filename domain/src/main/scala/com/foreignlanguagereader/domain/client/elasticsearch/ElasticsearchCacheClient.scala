package com.foreignlanguagereader.domain.client.elasticsearch

import cats.implicits._
import com.foreignlanguagereader.domain.client.common._
import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.{
  ElasticsearchCacheRequest,
  ElasticsearchSearchRequest,
  ElasticsearchSearchResponse
}
import javax.inject.{Inject, Singleton}
import org.elasticsearch.action.bulk.BulkRequest
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class ElasticsearchCacheClient @Inject() (
    val client: ElasticsearchClient,
    val queue: CacheQueue,
    implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  /**
    *
    * We cache in elasticsearch because some content sources have request rate limits.
    * This caching prevents us from using requests for things we have already searched for,
    * and puts a limit on the number of times we will retry a search that hasn't given us results before.
    *
    * @param requests The search requests to cache.
    * @param tag The class of T, captured at runtime. This is needed to make a Seq of an arbitrary type due to JVM type erasure.
    * @tparam T A case class with Reads[T] and Writes[T]
    * @return
    */
  def findFromCacheOrRefetch[T](
      requests: List[ElasticsearchSearchRequest[T]]
  )(implicit
      tag: ClassTag[T],
      reads: Reads[T],
      writes: Writes[T]
  ): Future[List[List[T]]] = {

    // Fork and join for getting each request
    requests
      .traverse(request =>
        client
          .doubleSearch[T, LookupAttempt](request.query)
          .value
          .map(result =>
            ElasticsearchSearchResponse.fromResult(request, result)
          )
          // This is where fetchers are called to get results if they aren't in elasticsearch
          // There is also logic to remember what has been fetched from external sources
          // So that we don't try too many times on the same query
          .map(_.getResultOrFetchFromSource)
          // Future of a future can just wait until both complete
          .flatten
      )
      // Block here until we have finished with all of the requests
      .map(results => {
        // Asychronously save results back to elasticsearch
        // toIndex only exists when results were fetched from the fetchers
        val toSave = results.flatMap(_.toIndex).flatten

        if (toSave.nonEmpty) {
          queue.addInsertsToQueue(toSave)

          // Spin up a thread to work on the queue
          // And return to user without waiting for it to finish
          val _ = startInserting()
          logger.info(s"Saving results back to elasticsearch")
        }

        results.map(_.result)
      })
  }

  def save(request: ElasticsearchCacheRequest): Future[Unit] = {
    logger.info(s"Saving to elasticsearch: $request")
    val bulkRequest = request.requests.foldLeft(new BulkRequest()) {
      case (acc, req) =>
        // A little clumsy but we need to preserve whether this is an index or an update to compile.
        // Either way, the action needed is the same.
        req match {
          case Left(i)  => acc.add(i)
          case Right(u) => acc.add(u)
        }
    }
    client.bulk(bulkRequest).value.map {
      case CircuitBreakerAttempt(_) =>
        logger
          .info(s"Successfully saved to elasticsearch: $request")
      case CircuitBreakerNonAttempt() =>
        logger
          .info(
            "Elasticsearch connection is unhealthy, retrying insert when it is healthy."
          )
        queue.addInsertToQueue(request)
      case CircuitBreakerFailedAttempt(e) =>
        logger.warn(
          s"Failed to persist request to elasticsearch: $request, ${e.getMessage}",
          e
        )
        request.retries match {
          case Some(retries) =>
            logger.info(s"Retrying request individually: $request")
            queue.addInsertsToQueue(retries)
          case None =>
            logger.warn(
              s"Not retrying request because this is our second attempt: $request"
            )
        }
    }
  }

  // The breaker guard is pretty important, or else we just infinitely retry insertions
  // That will quickly consume the thread pool.
  def startInserting(): Future[Unit] =
    Future.apply {
      while (client.breaker.isClosed && queue.hasMore) {
        // We could rely on the check for queue emptiness but that opens us up to race conditions.
        //  Removing and checking is atomic, and the queue is thread safe, so we are protected.
        queue.nextInsert() match {
          case Some(item) =>
            save(item)
          case None =>
            logger.info("Finished inserting")
        }
      }
    }

  // Restart the queue when the circuit breaker starts working again.
  client.onClose({
    logger.info("Retrying inserts because elasticsearch is healthy again.")
    val _ = Future.apply(startInserting())
  })
}