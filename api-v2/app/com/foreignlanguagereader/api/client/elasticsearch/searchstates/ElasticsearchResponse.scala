package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.searches.{
  MultiSearchResponse,
  SearchResponse
}
import com.sksamuel.elastic4s.{ElasticError, HitReader, Indexable}
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

case class ElasticsearchResponse[T: Indexable](
  index: String,
  fields: Seq[Tuple2[String, String]],
  fetcher: () => Future[Option[Seq[T]]],
  maxFetchAttempts: Int,
  response: Option[MultiSearchResponse]
)(implicit hitReader: HitReader[T], tag: ClassTag[T], ec: ExecutionContext) {
  val logger: Logger = Logger(this.getClass)

  val (elasticsearchResult: Option[Seq[T]], fetchAttempts: Int) =
    response match {
      case Some(r) =>
        (parseResults(r.items(0).response), parseAttempts(r.items(1).response))
      case None => (None, 0)
    }

  def getResult: Future[ElasticsearchResult[T]] = elasticsearchResult match {
    case Some(es) =>
      Future.successful(
        ElasticsearchResult(
          index,
          fields,
          Some(es),
          fetchAttempts,
          refetched = false
        )
      )
    case None if fetchAttempts < maxFetchAttempts =>
      fetcher().map(
        result =>
          ElasticsearchResult(
            index,
            fields,
            result,
            fetchAttempts + 1,
            refetched = true
        )
      )
    case None =>
      Future.successful(
        ElasticsearchResult(
          index,
          fields,
          None,
          fetchAttempts,
          refetched = false
        )
      )
  }

  private[this] def parseResults(
    results: Either[ElasticError, SearchResponse]
  ): Option[Seq[T]] = results match {
    case Left(error) =>
      logger.error(
        s"Failed to get result from elasticsearch on index $index due to error ${error.reason}",
        error.asException
      )
      None
    case Right(response) =>
      val results = response.hits.hits.map(_.to[T])
      if (results.nonEmpty) Some(results.toIndexedSeq) else None
  }

  private[this] def parseAttempts(
    attempts: Either[ElasticError, SearchResponse]
  ) = attempts match {
    case Left(error) =>
      logger.error(
        s"Failed to get request count from elasticsearch on index $index due to error ${error.reason}",
        error.asException
      )
      0
    case Right(response) => response.hits.hits(0).to[LookupAttempt].count
  }
}

object ElasticsearchResponse {
  def fromResult[T: Indexable](request: ElasticsearchRequest[T],
                               result: Option[MultiSearchResponse])(
    implicit hitReader: HitReader[T],
    tag: ClassTag[T],
    ec: ExecutionContext
  ): ElasticsearchResponse[T] = {
    ElasticsearchResponse(
      request.index,
      request.fields,
      request.fetcher,
      request.maxFetchAttempts,
      result
    )
  }
}
