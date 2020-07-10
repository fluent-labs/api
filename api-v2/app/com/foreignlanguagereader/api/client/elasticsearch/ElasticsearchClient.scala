package com.foreignlanguagereader.api.client.elasticsearch

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.contentsource.definition.DefinitionEntry
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus.ReadinessStatus
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.requests.searches.queries.BoolQuery
import com.sksamuel.elastic4s.requests.searches.{SearchRequest, SearchResponse}
import javax.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

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
  val definitionsIndex = "definitions"
  val maxConcurrentInserts = 5
  val maxFetchRetries = 5

  def checkConnection(timeout: Duration): ReadinessStatus = {
    Try(Await.result(client.execute(indexExists(definitionsIndex)), timeout)) match {
      case Success(result) =>
        result match {
          case RequestSuccess(_, _, _, result) if result.exists =>
            ReadinessStatus.UP
          case RequestSuccess(_, _, _, _) =>
            logger.error(
              s"Error connecting to elasticsearch: index $definitionsIndex does not exist"
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

  def getDefinition(
    wordLanguage: Language,
    definitionLanguage: Language,
    word: String,
    source: DefinitionSource,
    fetcher: () => Future[Option[Seq[DefinitionEntry]]]
  ): Future[Option[Seq[DefinitionEntry]]] = {
    cacheWithElasticsearch[DefinitionEntry, Tuple3[Language, Language, String]](
      definitionsIndex,
      List(
        ("wordLanguage", wordLanguage.toString),
        ("definitionLanguage", definitionLanguage.toString),
        ("token", word),
        ("source", source.toString)
      ),
      fetcher
    )
  }

  private[this] def cacheWithElasticsearch[T: Indexable, U](
    index: String,
    fields: Seq[Tuple2[String, String]],
    fetcher: () => Future[Option[Seq[T]]]
  )(implicit hitReader: HitReader[T],
    handler: Handler[SearchRequest, SearchResponse],
    tag: ClassTag[T]): Future[Option[Seq[T]]] = {
    val query = boolQuery().must(fields.map {
      case (field, value) => matchQuery(field, value)
    })
    get(index, boolQuery().must(query)) match {
      // Happy case - we already have this fetched
      case Some(x) => Future.successful(Some(x))
      case None    =>
        // Check to make sure we haven't tried this search too many times
        getAttempts(index, fields) match {
          case n if n < maxFetchRetries =>
            Try(fetcher()) match {
              case Success(result) =>
                result map {
                  case Some(y) =>
                    save(index, y)
                    Some(y)
                  case None =>
                    saveAttempts(index, fields, n + 1)
                    None
                }
              case Failure(error) =>
                logger.error(
                  s"Failed to refresh data on index: $index with query $query: $error"
                )
                saveAttempts(index, fields, n + 1)
                Future.successful(None)
            }
          // If we've tried thi
          case _ =>
            logger.warn(
              s"Not fetching on index=$index for fields=$fields because maximum number of attempts has been exceeded"
            )
            Future.successful(None)
        }

    }
  }

  // Why Type tag? Runtime information needed to make a seq of an arbitrary type.
  // JVM type erasure would remove this unless we pass it along this way
  private[this] def get[T](index: String, query: BoolQuery)(
    implicit hitReader: HitReader[T],
    handler: Handler[SearchRequest, SearchResponse],
    tag: ClassTag[T]
  ): Option[Seq[T]] = {
    val request: Future[Response[SearchResponse]] =
      client.execute[SearchRequest, SearchResponse]({
        search(index).query(query)
      })

    Try(Await.result(request, elasticSearchTimeout)) match {
      case Success(result) =>
        result match {
          case RequestSuccess(_, _, _, result) =>
            val results = result.hits.hits.map(_.to[T])
            if (results.nonEmpty) Some(results.toIndexedSeq) else None
          case RequestFailure(_, _, _, error) =>
            logger.error(
              s"Error fetching on $index from elasticsearch due to error: ${error.reason}",
              error.asException
            )
            None
        }
      case Failure(e) =>
        logger.warn(
          s"Error fetching on $index from elasticsearch due to exception: ${e.getMessage}",
          e
        )
        None
    }
  }

  private[this] def getAttempts(index: String,
                                fields: Seq[Tuple2[String, String]]): Int = {
    1
  }

  private[this] def saveAttempts(index: String,
                                 fields: Seq[Tuple2[String, String]],
                                 count: Int): Unit = {
    val request = client.execute {
      indexInto(attemptsIndex)
        .doc(LookupAttempt(index = index, fields = fields, count = count))
    }

    Try(Await.result(request, elasticSearchTimeout)) match {
      case Failure(error) =>
        logger.error(
          s"Failed to save record of fetch attempts for index=$index and fields=$fields: $error"
        )
      case _ =>
        logger.info(
          s"Saved record of fetch attempts for index=$index and fields=$fields"
        )
    }

  }

  private[this] def save[T: Indexable](index: String, values: Seq[T]): Unit = {
    values
      .grouped(maxConcurrentInserts)
      .foreach(
        batch =>
          // Todo timeout
          Try(client.execute {
            bulk(
              batch
                .map(item => indexInto(index).doc(item))
            )
          }) match {
            case Success(s) =>
              s.foreach(
                r =>
                  logger.info(s"Successfully saved to elasticsearch: ${r.body}")
              )
            case Failure(e) =>
              logger.warn(s"Failed to persist to elasticsearch: $values", e)
        }
      )
  }
}
