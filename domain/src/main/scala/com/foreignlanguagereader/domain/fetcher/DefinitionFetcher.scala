package com.foreignlanguagereader.domain.fetcher

import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.external.definition.DefinitionEntry
import io.fluentlabs.content.types.internal.definition.Definition
import io.fluentlabs.content.types.internal.definition.DefinitionSource.DefinitionSource
import io.fluentlabs.content.types.internal.word.PartOfSpeech.PartOfSpeech
import io.fluentlabs.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.circuitbreaker.CircuitBreakerResult
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchCacheClient
import com.foreignlanguagereader.domain.client.elasticsearch.searchstates.ElasticsearchSearchRequest
import com.foreignlanguagereader.domain.metrics.MetricsReporter
import play.api.Logger
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait DefinitionFetcher[T <: DefinitionEntry, U <: Definition] {
  val logger: Logger = Logger(this.getClass)
  val metrics: MetricsReporter

  def fetch(
      language: Language,
      word: Word
  )(implicit ec: ExecutionContext): Future[CircuitBreakerResult[List[T]]]
  def convertToDefinition(entry: T, tag: PartOfSpeech): U
  implicit val reads: Reads[T]
  implicit val writes: Writes[T]

  val maxFetchAttempts = 5

  def fetchDefinitions(
      elasticsearch: ElasticsearchCacheClient,
      index: String,
      definitionLanguage: Language,
      wordLanguage: Language,
      source: DefinitionSource,
      word: Word
  )(implicit ec: ExecutionContext, tag: ClassTag[T]): Future[List[U]] = {
    metrics.reportDefinitionsSearchedInCache(source)
    elasticsearch
      .findFromCacheOrRefetch(
        makeDefinitionRequest(
          source,
          definitionLanguage,
          wordLanguage,
          index,
          word
        )
      )
      .map(entries => {
        logger
          .info(s"Received results from elasticsearch for word $word: $entries")
        entries.map(entry => convertToDefinition(entry, word.tag))
      })
  }

  // Definition domain to elasticsearch domain
  private[this] def makeDefinitionRequest(
      source: DefinitionSource,
      definitionLanguage: Language,
      wordLanguage: Language,
      index: String,
      word: Word
  )(implicit ec: ExecutionContext): ElasticsearchSearchRequest[T] = {
    val fetcher = () => {
      metrics.reportDefinitionsNotFoundInCache(source)
      fetch(definitionLanguage, word)
    }

    ElasticsearchSearchRequest(
      index,
      Map(
        "wordLanguage" -> wordLanguage.toString,
        "definitionLanguage" -> definitionLanguage.toString,
        "token" -> word.processedToken,
        "source" -> source.toString
      ),
      fetcher,
      maxFetchAttempts
    )
  }
}
