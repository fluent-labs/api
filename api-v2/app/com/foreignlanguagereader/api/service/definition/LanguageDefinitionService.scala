package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.contentsource.definition.DefinitionEntry
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource
}
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
  val sources: Set[DefinitionSource]

  // These are strongly recommended to implement, but have sane defaults

  // Pre-request hook for normalizing user requests. Suggested for lemmatization
  // Eg: run, runs, running all become run so you don't keep re-teaching the same word
  def preprocessTokenForRequest(token: String): String = token

  // Functions that can fetch definitions from web sources should be registered here.
  val definitionFetchers
    : Map[(DefinitionSource, Language), (Language, String) => Future[
      Option[Seq[DefinitionEntry]]
    ]] = Map(
    (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
  )

  // Define logic to combine definition sources here. If there is only one source, this just returns it.
  def enrichDefinitions(definitionLanguage: Language,
                        word: String,
                        definitions: Seq[Definition]): Seq[Definition] =
    definitions

  // This is the main method that consumers will call
  def getDefinitions(definitionLanguage: Language,
                     word: String): Future[Option[Seq[Definition]]] = {
    fetchDefinitions(
      sources,
      definitionLanguage,
      preprocessTokenForRequest(word)
    ) map {
      case Some(definitions) =>
        Some(enrichDefinitions(definitionLanguage, word, definitions))
      case None =>
        logger.info(
          s"No definitions found for $wordLanguage $word in $definitionLanguage"
        )
        None
    }
  }

  // Below here is trait behavior, implementers need not read further

  val definitionsIndex = "definitions"

  // Checks registered definition fetchers and uses them
  private[this] def fetchDefinition(
    source: DefinitionSource,
    definitionLanguage: Language,
    word: String
  ): Future[Option[Seq[Definition]]] = {

    definitionFetchers.get(source, definitionLanguage) match {
      case Some(fetcher) =>
        elasticsearch
          .cacheWithElasticsearch[Definition, Tuple3[Language,
                                                     Language,
                                                     String]](
            definitionsIndex,
            List(
              ("wordLanguage", wordLanguage.toString),
              ("definitionLanguage", definitionLanguage.toString),
              ("token", word),
              ("source", source.toString)
            ),
            () =>
              fetcher(definitionLanguage, word).map {
                case Some(results) => Some(results.map(_.toDefinition))
                case None          => None
            }
          )
      case None =>
        logger.error(
          s"Failed to search in $wordLanguage for $word because $source is not implemented for definitions in $definitionLanguage"
        )
        Future.successful(None)
    }
  }

  // Out of the box, this calls language service for Wiktionary definitions. All languages should use this.
  def languageServiceFetcher
    : (Language, String) => Future[Option[Seq[DefinitionEntry]]] =
    (_, word: String) => languageServiceClient.getDefinition(wordLanguage, word)

  // Convenience method to request multiple sources in parallel
  private[this] def fetchDefinitions(
    sources: Set[DefinitionSource],
    definitionLanguage: Language,
    word: String
  ): Future[Option[Seq[Definition]]] =
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
            case empty if empty.isEmpty => None
            // Combine all the results together
            case s =>
              s.reduce(_ ++ _) match {
                case e if e.isEmpty => None
                case d              => Some(d)
              }
        }
      )
}
