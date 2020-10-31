package com.foreignlanguagereader.api.service.definition

import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.elasticsearch.searchstates.ElasticsearchSearchRequest
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.domain.internal.word.Word
import com.sksamuel.elastic4s.playjson._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/*
 * Core functionality for getting definitions for a given language.
 * Allows each language to specify only what is specific to it.
 *
 * Dictionaries come in two forms: file and web
 * File dictionaries will be loaded into elasticsearch, and only fetched there.
 * Web dictionaries can be scraped on request, but should be cached in elasticsearch.
 *
 * Implementing a language means defining three behaviors:
 * - preprocessTokenForRequest lets you do any normalizing of a word before searching. Eg: lemmatization (run, runs, running become the same word)
 * - enrichDefinitions will combine multiple dictionaries into one set of definitions based on the quality of the inputs.
 * - Adding any language specific sources into the definitionFetchers. By default Wiktionary is provided to all languages
 *
 * There are also inputs you need to provide:
 * sources: Set of all the dictionaries used for this language
 * webSources: Which sources are web sources
 * wordLanguage: Which language this service is implementing
 * ec: A thread pool to use. Since none of these methods are blocking, any pool is fine.
 * elasticsearch: An ElasticsearchClient for use in caching and getting file sources
 */
trait LanguageDefinitionService {
  // These are required fields for implementers
  implicit val ec: ExecutionContext
  val logger: Logger = Logger(this.getClass)
  val elasticsearch: ElasticsearchClient
  val languageServiceClient: LanguageServiceClient
  val wordLanguage: Language
  val sources: List[DefinitionSource]

  // These are strongly recommended to implement, but have sane defaults

  // Pre-request hook for normalizing user requests. Suggested for lemmatization
  // Eg: run, runs, running all become run so you don't keep re-teaching the same word
  def preprocessWordForRequest(word: Word): List[Word] = List(word)

  // Functions that can fetch definitions from web sources should be registered here.
  val definitionFetchers: Map[
    (DefinitionSource, Language),
    (Language, Word) => Nested[Future, CircuitBreakerResult, List[Definition]]
  ] =
    Map(
      (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
    )
  val maxFetchAttempts = 5

  // Define logic to combine definition sources here. If there is only one source, this just returns it.
  def enrichDefinitions(
      definitionLanguage: Language,
      word: Word,
      definitions: Map[DefinitionSource, List[Definition]]
  ): List[Definition] = definitions.values.flatten.toList

  // This is the main method that consumers will call
  def getDefinitions(
      definitionLanguage: Language,
      word: Word
  ): Future[List[Definition]] = {
    preprocessWordForRequest(word)
    // Trigger all requests asynchronously and block for all to complete
      .traverse(token => fetchDefinitions(sources, definitionLanguage, token))
      // Merge definitions for each lookup term
      .map(_.combineAll)
      // Remove empty sources
      .map(p =>
        p.collect {
          case (k, v) if v.nonEmpty => k -> v
        }
      )
      .map(definitions =>
        enrichDefinitions(definitionLanguage, word, definitions)
      )
  }

  // Below here is trait behavior, implementers need not read further

  val definitionsIndex = "definitions"

  // Out of the box, this calls language service for Wiktionary definitions. All languages should use this.
  def languageServiceFetcher: (
      Language,
      Word
  ) => Nested[Future, CircuitBreakerResult, List[Definition]] =
    (_, word: Word) => languageServiceClient.getDefinition(wordLanguage, word)

  private[this] def fetchDefinitions(
      sources: List[DefinitionSource],
      definitionLanguage: Language,
      word: Word
  ): Future[Map[DefinitionSource, List[Definition]]] = {
    elasticsearch
      .findFromCacheOrRefetch[Definition](
        sources
          .map(source =>
            makeDefinitionRequest(source, definitionLanguage, word)
          )
      )
      .map(results => sources.zip(results).toMap)
  }

  // Definition domain to elasticsearch domain
  private[this] def makeDefinitionRequest(
      source: DefinitionSource,
      definitionLanguage: Language,
      word: Word
  ): ElasticsearchSearchRequest[Definition] = {
    val fetcher: () => Future[CircuitBreakerResult[List[Definition]]] =
      definitionFetchers.get(source, definitionLanguage) match {
        case Some(fetcher) =>
          () => fetcher(definitionLanguage, word).value
        case None =>
          logger.error(
            s"Failed to search in $wordLanguage for $word because $source is not implemented for definitions in $definitionLanguage"
          )
          () => CircuitBreakerNonAttempt[List[Definition]]().pure[Future]
      }

    ElasticsearchSearchRequest(
      definitionsIndex,
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
