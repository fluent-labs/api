package io.fluentlabs.domain.client.elasticsearch.searchstates

import cats.data.Nested
import cats.syntax.all._
import io.fluentlabs.content.types.internal.ElasticsearchCacheable
import io.fluentlabs.domain.client.circuitbreaker.{
  CircuitBreakerAttempt,
  CircuitBreakerFailedAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import io.fluentlabs.domain.client.elasticsearch.LookupAttempt
import play.api.Logger
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/** Turns the raw elasticsearch response into the query result.
  * This decides whether we will need to refetch from the original content source.
  *
  * @param index The elasticsearch index to cache the data. Should just be the type
  * @param fields The fields needed to look up the correct item. Think of this as the primary key.
  * @param fetcher A function to be called if results are not in elasticsearch, which can try to get the results again.
  * @param maxFetchAttempts If we don't have any results, how many times should we search for this? Highly source dependent.
  * @param response The elasticsearch response created by using the query in ElasticsearchRequest
  * @param tag The class of T so that sequences can be initialized. Automatically given.
  * @param ec Automatically taken from the implicit val near the caller. This is the thread pool to block on when fetching.
  * @tparam T A case class.
  */
case class ElasticsearchSearchResponse[T](
    index: String,
    fields: Map[String, String],
    fetcher: () => Future[
      CircuitBreakerResult[List[T]]
    ],
    maxFetchAttempts: Int,
    response: CircuitBreakerResult[
      Option[
        (Map[String, T], Map[String, LookupAttempt])
      ]
    ]
)(implicit
    tag: ClassTag[T],
    reads: Reads[T],
    writes: Writes[T],
    ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

  val (
    elasticsearchResult: Option[Seq[T]],
    fetchCount: Int,
    lookupId: Option[String],
    queried: Boolean
  ) =
    response match {
      case CircuitBreakerAttempt(Some(r)) =>
        logger.info(
          s"Query on index $index with fields $fields received result $r"
        )

        val (results, attemptsResult) = r

        // Done to force us to refetch
        val actualResults = if (results.nonEmpty) {
          Some(results.values.toSeq)
        } else { None }

        val (id, attempts) = if (attemptsResult.nonEmpty) {
          val (docId, a) = attemptsResult.head
          (Some(docId), a.count)
        } else { (None, 0) }

        logger.info(
          s"Query on index $index with fields $fields received result $actualResults with attempts $attempts"
        )

        (actualResults, attempts, id, true)
      case _ =>
        logger.info(
          s"Query on index $index with fields $fields received no result. (First time attempted)"
        )
        (None, 0, None, false)
    }

  // Were we able to connect to elasticsearch?
  // Necessary downstream to prevent us from resaving to elasticsearch.

  lazy val getResultOrFetchFromSource: Future[ElasticsearchSearchResult[T]] =
    elasticsearchResult match {
      case Some(es) =>
        logger.info(
          s"Returning elasticsearch results for query on index $index with fields $fields"
        )
        Future.successful(
          ElasticsearchSearchResult(
            index = index,
            fields = fields,
            result = es.toList,
            fetchCount = fetchCount,
            lookupId = lookupId,
            refetched = false,
            queried = queried
          )
        )
      case None if fetchCount < maxFetchAttempts =>
        logger.info(
          s"Refetching from source for query on index $index with fields $fields"
        )
        fetchFromSource
      case None =>
        logger.info(
          s"Not refetching from source because maximum attempts have been exceeded for query on index $index with fields $fields"
        )
        Future.successful(
          ElasticsearchSearchResult(
            index = index,
            fields = fields,
            result = List(),
            fetchCount = fetchCount,
            lookupId = lookupId,
            refetched = false,
            queried = queried
          )
        )
    }

  lazy val fetchFromSource: Future[ElasticsearchSearchResult[T]] = {
    logger.info(s"Refetching from source for query on $index")
    fetcher()
      .map {
        case CircuitBreakerAttempt(result) =>
          logger.info(
            s"Successfully called fetcher on index=$index for fields=$fields"
          )
          ElasticsearchSearchResult(
            index = index,
            fields = fields,
            result = result,
            fetchCount = fetchCount + 1,
            lookupId = lookupId,
            refetched = true,
            queried = queried
          )
        case CircuitBreakerNonAttempt() =>
          logger.warn(
            s"Failed to call fetcher on index=$index for fields=$fields because the circuitbreaker was closed"
          )
          ElasticsearchSearchResult(
            index = index,
            fields = fields,
            result = List(),
            fetchCount = fetchCount,
            lookupId = lookupId,
            refetched = false,
            queried = queried
          )
        case CircuitBreakerFailedAttempt(e) =>
          logger.error(
            s"Failed to call fetcher on index=$index for fields=$fields due to error ${e.getMessage}",
            e
          )
          ElasticsearchSearchResult(
            index = index,
            fields = fields,
            result = List(),
            fetchCount = fetchCount + 1,
            lookupId = lookupId,
            refetched = true,
            queried = queried
          )
      }
      .recover { case e: Exception =>
        logger.error(
          s"Failed to call fetcher on index=$index for fields=$fields due to error ${e.getMessage}",
          e
        )
        ElasticsearchSearchResult(
          index = index,
          fields = fields,
          result = List(),
          fetchCount = fetchCount + 1,
          lookupId = lookupId,
          refetched = true,
          queried = queried
        )

      }
  }
}

object ElasticsearchSearchResponse {
  def fromResult[T](
      request: ElasticsearchSearchRequest[T],
      result: CircuitBreakerResult[
        Option[
          (Map[String, ElasticsearchCacheable[T]], Map[String, LookupAttempt])
        ]
      ]
  )(implicit
      read: Reads[T],
      writes: Writes[T],
      tag: ClassTag[T],
      ec: ExecutionContext
  ): ElasticsearchSearchResponse[T] = {
    // Basically removes the ElasticsearchCacheable[] outer wrapper
    val unwrappedResult = Nested
      .apply(result)
      .map { case (items, attempts) =>
        (items.mapValues(_.item), attempts)
      }
      .value

    ElasticsearchSearchResponse(
      request.index,
      request.fields,
      request.fetcher,
      request.maxFetchAttempts,
      unwrappedResult
    )
  }
}
