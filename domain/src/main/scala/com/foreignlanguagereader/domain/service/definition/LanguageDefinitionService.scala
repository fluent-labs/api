package com.foreignlanguagereader.domain.service.definition

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.Definition
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerFailedAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchCacheClient
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import play.api.{Configuration, Logger}

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
trait LanguageDefinitionService[T <: Definition] {
  // These are required fields for implementers
  implicit val ec: ExecutionContext
  val config: Configuration
  val logger: Logger = Logger(this.getClass)
  val elasticsearch: ElasticsearchCacheClient
  val wordLanguage: Language
  val sources: List[DefinitionSource]

  // These are strongly recommended to implement, but have sane defaults

  // Pre-request hook for normalizing user requests. Suggested for lemmatization
  // Eg: run, runs, running all become run so you don't keep re-teaching the same word
  def preprocessWordForRequest(word: Word): List[Word] = List(word)

  // Functions that can fetch definitions from web sources should be registered here.
  val definitionFetchers
      : Map[(DefinitionSource, Language), DefinitionFetcher[_, T]] = Map()
  val maxFetchAttempts = 5

  // Define logic to combine definition sources here. If there is only one source, this just returns it.
  def enrichDefinitions(
      definitionLanguage: Language,
      word: Word,
      definitions: Map[DefinitionSource, List[T]]
  ): List[T] = definitions.values.flatten.toList

  // This is the main method that consumers will call
  def getDefinitions(
      definitionLanguage: Language,
      word: Word
  ): Future[List[T]] =
    Future
      .traverse(sources)(source =>
        getDefinitionsForSingleSource(definitionLanguage, source, word).map(
          results => {
            logger.info(
              s"Got definitions for word $word source $source: $results"
            )
            source -> results
          }
        )
      )
      .map(_.toMap)
      // Remove empty sources
      .map(p =>
        p.collect {
          case (k, v) if v.nonEmpty => k -> v
        }
      )
      .map(definitions =>
        if (definitions.nonEmpty) {
          logger.info(
            s"Enriching definitions in $definitionLanguage for word $word"
          )
          enrichDefinitions(definitionLanguage, word, definitions)
        } else {
          logger.info(
            s"Not enriching definitions in $definitionLanguage for word $word because no results were found"
          )
          List()
        }
      )

  // Below here is trait behavior, implementers need not read further

  private[this] def getDefinitionsForSingleSource(
      definitionLanguage: Language,
      source: DefinitionSource,
      word: Word
  ): Future[List[T]] = {
    Future
      .traverse(preprocessWordForRequest(word))(token =>
        getDefinitionsForToken(definitionLanguage, source, token)
      )
      .map(_.flatten)
  }

  private[this] def getDefinitionsForToken(
      definitionLanguage: Language,
      source: DefinitionSource,
      word: Word
  ): Future[List[T]] =
    definitionFetchers.get((source, definitionLanguage)) match {
      case None =>
        logger.warn(
          s"Fetcher not implemented for source $source in language $definitionLanguage"
        )
        Future.successful(List[T]())
      case Some(fetcher) =>
        fetcher
          .fetchDefinitions(
            elasticsearch,
            getIndex,
            definitionLanguage,
            wordLanguage,
            source,
            word
          )
    }

  // Use this for definitions not backed by a rest API: eg loaded using spark
  def elasticsearchDefinitionClient[U <: DefinitionEntry](
      language: Language,
      word: Word
  ): Future[CircuitBreakerResult[List[U]]] =
    Future.apply(
      CircuitBreakerFailedAttempt(
        new NotImplementedError(
          s"Fetcher not implemented for language: $language, failed to fetch $word"
        )
      )
    )

  def getIndex: String = {
    s"definitions-${config.get[String]("environment")}"
  }
}
