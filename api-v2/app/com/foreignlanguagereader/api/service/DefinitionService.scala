package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition
}
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionEntry,
  WiktionaryDefinitionEntry
}
import javax.inject.{Inject, Singleton}
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  implicit val ec: ExecutionContext
) {
  val logger: Logger = Logger(this.getClass)

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
    findOrFetchDefinition(wordLanguage, definitionLanguage, word) map {
      case None => None
      // Special case: Combine wiktionary and cedict sources for richer output
      case Some(d) if wordLanguage == Language.CHINESE =>
        logger.info(s"Parsing Chinese definitions for $word")
        enrichChineseDefinitionsWithCEDICTData(word, d)
      case Some(d) =>
        logger.info(s"Parsing generic definitions for $word in $wordLanguage")
        Some(DefinitionEntry.convertToSeqOfDefinitions(d))
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
    *
    * This method should only be called if a CEDICT entry is in this list, and will throw an exception otherwise.
    * If you don't have a CEDICT, then you don't need to augment data anyway, and you should use the mappers.
    *
    * @param definitions The definitions to parse
    * @return
    */
  def enrichChineseDefinitionsWithCEDICTData(
    word: String,
    definitions: Seq[DefinitionEntry]
  ): Option[Seq[Definition]] = {
    // partition the definitions into their respective sources so we can work on them easily
    val (
      cedict: List[CEDICTDefinitionEntry],
      wiktionary: List[WiktionaryDefinitionEntry]
    ) = definitions.foldLeft(
      (List[CEDICTDefinitionEntry](), List[WiktionaryDefinitionEntry]())
    )(
      (acc, entry) =>
        entry match {
          case c: CEDICTDefinitionEntry     => (c :: acc._1, acc._2)
          case w: WiktionaryDefinitionEntry => (acc._1, w :: acc._2)
      }
    )
    logger.info(
      s"Enhancing results for $word using cedict with ${cedict.size} cedict results and ${wiktionary.size} wiktionary results"
    )

    (cedict, wiktionary) match {
      case (cedict, wiktionary) if cedict.isEmpty && wiktionary.isEmpty => None
      case (cedict, wiktionary) if wiktionary.isEmpty =>
        Some(cedict.map(_.toDefinition))
      case (cedict, wiktionary) if cedict.isEmpty =>
        Some(wiktionary.map(_.toDefinition))
      // If CEDICT doesn't have subdefinitions, then we should return wiktionary data
      // We still want pronunciation and simplified/traditional mapping, so we will add cedict data
      case (cedict, wiktionary) if cedict(0).subdefinitions.isEmpty =>
        logger.info(s"Using wiktionary definitions for $word")
        val c = cedict(0)
        Some(
          wiktionary.map(
            w =>
              ChineseDefinition(
                w.subdefinitions,
                w.tag,
                w.examples,
                c.pinyin,
                c.simplified,
                c.traditional
            )
          )
        )
      // If are definitions from CEDICT, they are better.
      // In that case, we only want part of speech tag and examples from wiktionary.
      // But everything else will be the single CEDICT definition
      case (cedict, wiktionary) =>
        logger.info(s"Using cedict definitions for $word")
        val examples = wiktionary.foldLeft(List[String]())(
          (acc, entry: WiktionaryDefinitionEntry) => {
            acc ++ entry.examples
          }
        )
        val c = cedict(0)
        Some(
          Seq(
            ChineseDefinition(
              c.subdefinitions,
              wiktionary(0).tag,
              examples,
              c.pinyin,
              c.simplified,
              c.traditional
            )
          )
        )
    }
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
    fetchDefinitionsFromElasticsearch(wordLanguage, definitionLanguage, word) match {
      case None =>
        logger.info(
          s"Using language service definitions for $word in $wordLanguage"
        )
        languageServiceClient
          .getDefinition(wordLanguage, definitionLanguage, word)
      case Some(definitions) =>
        logger.info(
          s"Using elasticsearch definitions for $word in $wordLanguage"
        )
        Future.successful(Some(definitions))
    }

  def fetchDefinitionsFromElasticsearch(
    wordLanguage: Language,
    definitionLanguage: Language,
    word: String
  ): Option[Seq[DefinitionEntry]] = {
    try {
      elasticsearch.getDefinition(wordLanguage, definitionLanguage, word)
    } catch {
      case e: Exception =>
        logger.warn(
          s"Failed to get definitions in $language for word $word from elasticsearch: ${e.getMessage}",
          e
        )
        None
    }
  }
}
