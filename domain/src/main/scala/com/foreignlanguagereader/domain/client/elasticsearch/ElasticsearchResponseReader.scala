package com.foreignlanguagereader.domain.client.elasticsearch

import cats.implicits._
import org.elasticsearch.action.search.{MultiSearchResponse, SearchResponse}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}

// $COVERAGE-OFF$
// This class exists to mock results from the elasticsearch client,
// which has been created in a way that prevents mocking.
// The scenarios we care about is how we react to elasticsearch results,
// not the details of the fetching
class ElasticsearchResponseReader {
  val logger: Logger = Logger(this.getClass)

  def getResultsFromSearch[T](response: MultiSearchResponse)(implicit
      reads: Reads[T]
  ): (Option[Seq[T]], Int, Option[String]) = {
    val responses = response.getResponses
    val results = getResultsFromMultisearchItem[T](responses.head) match {
      case Left(hits) =>
        Some(hits.map {
          case (_, item) => item
        })
      case Right(_) => None
    }
    val (attemptsCount, attemptsId) =
      getResultsFromMultisearchItem[LookupAttempt](responses.tail.head) match {
        case Left(a) =>
          val (id, attempt) = a.head
          (attempt.count, Some(id))
        case Right(_) => (0, None)
      }

    (results, attemptsCount, attemptsId)
  }

  def getResultsFromMultisearchItem[T](
      item: MultiSearchResponse.Item
  )(implicit reads: Reads[T]): Either[Seq[(String, T)], Exception] = {
    if (item.isFailure) {
      logger.error(
        s"Failed to get request count from elasticsearch due to error ${item.getFailureMessage}",
        item.getFailure
      )
      item.getFailure.asRight
    } else {
      getHitsFrom(item.getResponse) match {
        case Some(results) => results.asLeft
        case None =>
          new IllegalStateException(
            "Failed to parse json from elasticsearch"
          ).asRight
      }
    }
  }

  def getHitsFrom[T](
      response: SearchResponse
  )(implicit reads: Reads[T]): Option[Seq[(String, T)]] = {
    val r = response.getHits.getHits
      .map(hit =>
        Json.parse(hit.getSourceAsString).validate[T] match {
          case JsSuccess(value, _) => Some((hit.getId, value))
          case JsError(errors) =>
            val errs = errors
              .map {
                case (path, e) => s"At path $path: ${e.mkString(", ")}"
              }
              .mkString("\n")
            logger.error(
              s"Failed to parse results from elasticsearch due to errors: $errs"
            )
            None
        }
      )

    if (r.forall(_.isDefined)) { Some(r.flatten.toList) }
    else { None }
  }
}
// $COVERAGE-ON$
