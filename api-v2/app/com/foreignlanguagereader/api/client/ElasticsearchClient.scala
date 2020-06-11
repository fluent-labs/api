package com.foreignlanguagereader.api.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionEntry
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus
import com.foreignlanguagereader.api.dto.v1.health.ReadinessStatus.ReadinessStatus
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{
  ElasticClient,
  ElasticProperties,
  RequestFailure,
  RequestSuccess,
  Response
}
import javax.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ElasticsearchClient @Inject()(config: Configuration,
                                    val system: ActorSystem) {
  val logger: Logger = Logger(this.getClass)

  // What's with all the awaits? elastic4s does not implement connection timeouts.
  // Instead, we need to implement them ourselves.
  // Not the end of the world, we would have been blocking here anyway since it is IO
  // We use this thread pool to keep it out of the main server threads
  implicit val myExecutionContext: ExecutionContext =
    system.dispatchers.lookup("elasticsearch-context")

  val elasticSearchUrl: String = config.get[String]("elasticsearch.url")
  val elasticSearchTimeout =
    Duration(config.get[Int]("elasticsearch.timeout"), TimeUnit.SECONDS)
  val client = ElasticClient(JavaClient(ElasticProperties(elasticSearchUrl)))

  val definitionsIndex = "definitions"

  import com.sksamuel.elastic4s.ElasticDsl._

  def checkConnection(timeout: Duration): ReadinessStatus =
    try {
      Await.result(client.execute(indexExists(definitionsIndex)), timeout) match {
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
    } catch {
      case e: Exception =>
        logger
          .error(s"Failed to connect to elasticsearch: ${e.getMessage}", e)
        ReadinessStatus.DOWN
    }

  def getDefinition(wordLanguage: Language,
                    definitionLanguage: Language,
                    word: String): Option[Seq[DefinitionEntry]] = {
    try {
      val request: Future[Response[SearchResponse]] = client
        .execute({
          search(definitionsIndex).query(
            boolQuery()
              .must(
                matchQuery("language", wordLanguage.toString),
                matchQuery("token", word)
              )
          )
        })

      Await.result(request, elasticSearchTimeout) match {
        case RequestSuccess(_, _, _, result) =>
          val results = result.hits.hits.map(_.to[DefinitionEntry])
          if (results.nonEmpty) Some(results.toIndexedSeq) else None
        case RequestFailure(_, _, _, error) =>
          logger.error(
            s"Error fetching definitions from elasticsearch: ${error.reason}",
            error.asException
          )
          None
      }
    } catch {
      case e: Exception =>
        logger.warn(
          s"Failed to get definitions in $language for word $word from elasticsearch: ${e.getMessage}",
          e
        )
        None
    }
  }

  def saveDefinitions(definition: Seq[DefinitionEntry]): Unit = ???
}
