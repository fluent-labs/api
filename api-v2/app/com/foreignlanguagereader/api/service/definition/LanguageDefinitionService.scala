package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.{
  DefinitionEntry,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
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
 * Implementing a language means defining two behaviors:
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
  val sources: Set[DefinitionSource]
  val webSources: Set[DefinitionSource]

  // These are strongly recommended to implement, but have sane defaults

  // Functions that can fetch definitions from web sources should be registered here.
  val definitionFetchers: Map[DefinitionSource, (Language, String) => Future[
    Option[Seq[DefinitionEntry]]
  ]] = Map(DefinitionSource.WIKTIONARY -> languageServiceFetcher)

  // Define logic to combine definition sources here. If there is only one source, this just returns it.
  def enrichDefinitions(
    definitionLanguage: Language,
    word: String,
    definitions: Seq[DefinitionEntry]
  ): Option[Seq[Definition]] = Some(definitions.map(e => e.toDefinition))

  // This is the main method that consumers will call
  def getDefinitions(definitionLanguage: Language,
                     word: String): Future[Option[Seq[Definition]]] = {
    findOrFetchDefinitions(definitionLanguage, word) map {
      case Some(definitions) =>
        enrichDefinitions(definitionLanguage, word, definitions)
      case None => None
    }
  }

  // Below here is trait behavior, implementers need not read further

  // Handles fetching definitions from elasticsearch, and getting sources that are missing
  private def findOrFetchDefinitions(
    definitionLanguage: Language,
    word: String
  ): Future[Option[Seq[DefinitionEntry]]] = {
    elasticsearch.getDefinition(wordLanguage, definitionLanguage, word) match {
      case Some(d) if missingWebSources(d).isEmpty =>
        logger.info(
          s"Using elasticsearch definitions for $word in $wordLanguage"
        )
        Future.successful(Some(d))
      case Some(d) =>
        val missing = missingWebSources(d)
        logger.info(
          s"Refreshing definitions for $word in $wordLanguage from $sources"
        )
        fetchDefinitions(missing, definitionLanguage, word) map {
          case Some(refetched) =>
            elasticsearch.saveDefinitions(refetched)
            Some(d ++ refetched)
          case None => Some(d)
        }
      // Is this just a special case of Some(d) where there are missing sources?
      case None =>
        logger.info(
          s"Refreshing definitions for $word in $wordLanguage using all sources"
        )
        fetchDefinitions(webSources, definitionLanguage, word).map {
          case Some(d) =>
            elasticsearch.saveDefinitions(d)
            Some(d)
          case None => None
        }
    }
  }

  // Which sources can be refreshed which we don't have data for?
  private def missingWebSources(
    definitions: Seq[DefinitionEntry]
  ): Set[DefinitionSource] =
    webSources.filterNot(source => definitions.exists(_.source == source))

  // Checks registered definition fetchers and uses them
  private def fetchDefinition(
    source: DefinitionSource,
    definitionLanguage: Language,
    word: String
  ): Future[Option[Seq[DefinitionEntry]]] = {
    definitionFetchers.get(source) match {
      case Some(fetcher) => fetcher(definitionLanguage, word)
      case None =>
        logger.error(
          s"Tried to fetch from unimplemented source $source in $wordLanguage for $word"
        )
        Future.successful(None)
    }
  }

  // Out of the box, this calls language service for Wiktionary definitions. All languages should use this.
  def languageServiceFetcher
    : (Language, String) => Future[Option[Seq[DefinitionEntry]]] =
    (definitionLanguage: Language, word: String) =>
      languageServiceClient.getDefinition(
        wordLanguage,
        definitionLanguage,
        word
    )

  // Convenience method to request multiple sources in parallel
  private def fetchDefinitions(
    sources: Set[DefinitionSource],
    definitionLanguage: Language,
    word: String
  ): Future[Option[Seq[DefinitionEntry]]] = {
    // Fire off all the results
    Future
      .sequence(
        sources.map(source => fetchDefinition(source, definitionLanguage, word))
      )
      // Wait until completion
      .map(
        sources =>
          // Remove all empty results
          sources.flatten match {
            // No results found
            case s if s.isEmpty => None
            // Combine all the results together
            case s =>
              s.reduce(_ ++ _) match {
                case d if d.isEmpty => None
                case d              => Some(d)
              }
        }
      )
  }
}