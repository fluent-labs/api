package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.{FutureCollector, Language}
import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition
}
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionEntry,
  DefinitionSource
}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  implicit val ec: ExecutionContext,
  val fc: FutureCollector
) {

  /**
    * Gets definitions for a list of tokens
    * Definition language currently only supports English but in the future this won't be so.
    * @param wordLanguage Which language the token is in
    * @param definitionLanguage What language the definitions should be in.
    * @param word The token to search for.
    * @return
    */
  def getDefinition(wordLanguage: Language,
                    definitionLanguage: Language,
                    word: String): Future[Option[Seq[Definition]]] =
    findOrFetchDefinition(definitionLanguage, wordLanguage, word) map {
      case None => None
      case Some(d) if wordLanguage == Language.CHINESE =>
        Some(transformChineseDefinitions(d))
      case Some(d) => Some(d)
    }

  // Convenience method for getting definitions in parallel.
  // Same interface as above
  def getDefinitions(
    wordLanguage: Language,
    definitionLanguage: Language,
    words: List[String]
  ): Future[Map[String, Option[Seq[Definition]]]] = {
    // Start by requesting everything asynchronously.
    Future
      .sequence(
        words.map(word => getDefinition(wordLanguage, definitionLanguage, word))
      )
      // Remove empties
      .map(_.flatten)
      // Match words to definitions based on tokens
      .map(_.map(definitions => {
        val word: Definition = definitions(0)
        word.token -> Some(definitions)
      }).toMap)
      // Include anything that wasn't found as an empty list to not confuse callers
      .map(definitions => {
        val foundWords = definitions.keySet
        val missingWords = words.toSet.diff(foundWords)
        definitions ++ missingWords
          .map(word => word -> None)
          .toMap
      })
  }

  /**
    * Language specific handling for Chinese.
    * We have two dictionaries here, so we should combine them to produce the best possible results
    * In particular, CEDICT has a minimum level of quality, but doesn't have as many definitions.
    * We prefer CEDICT when available
    * @param definitions The definitions returned from elasticsearch
    * @return
    */
  def transformChineseDefinitions(
    definitions: Seq[DefinitionEntry]
  ): Seq[ChineseDefinition] = {

    // There is only one CEDICT definition so we should use that.
    val CEDICTDefinition: Option[CEDICTDefinitionEntry] = definitions
      .find(_.source.equals(DefinitionSource.CEDICT))
      .asInstanceOf[Option[CEDICTDefinitionEntry]]

    definitions
      .filterNot(_.source.equals(DefinitionSource.CEDICT))
      .iterator
      .map(definition => {
        // Cedict has more focused subdefinitions so we should prefer those.
        // Augment the rest of the definitions with CEDICT language specific data
        CEDICTDefinition match {
          case Some(cedict) =>
            ChineseDefinition(
              if (cedict.subdefinitions.nonEmpty) cedict.subdefinitions
              else definition.subdefinitions,
              definition.tag,
              definition.examples,
              cedict.pinyin,
              cedict.simplified,
              cedict.traditional
            )
          case None =>
            ChineseDefinition(
              definition.subdefinitions,
              definition.tag,
              definition.examples
            )
        }
      })
      .toList
  }

  // Long term roadmap is to have all language content in elasticsearch.
  // Right now it is not all there, so we should be prepared to scrape wiktionary for anything missing
  // And that functionality is in language service.
  // It's not worth rewriting it for an intermediate step, so we have this hack for now
  def findOrFetchDefinition(
    wordLanguage: Language,
    definitionLanguage: Language,
    word: String
  ): Future[Option[Seq[DefinitionEntry]]] =
    elasticsearch.getDefinition(wordLanguage, definitionLanguage, word) match {
      case None =>
        languageServiceClient
          .getDefinition(wordLanguage, definitionLanguage, word)
      case Some(definitions) => Future.successful(Some(definitions))
    }
}