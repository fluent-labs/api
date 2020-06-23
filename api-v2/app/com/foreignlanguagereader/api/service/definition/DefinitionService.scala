package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import javax.inject.{Inject, Singleton}
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionService @Inject()(
  val chineseDefinitionService: ChineseDefinitionService,
  val englishDefinitionService: EnglishDefinitionService,
  val spanishDefinitionService: SpanishDefinitionService,
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
    wordLanguage match {
      case Language.CHINESE =>
        chineseDefinitionService.getDefinitions(definitionLanguage, word)
      case Language.ENGLISH =>
        englishDefinitionService.getDefinitions(definitionLanguage, word)
      case Language.SPANISH =>
        spanishDefinitionService.getDefinitions(definitionLanguage, word)
    }

  // Convenience method for getting definitions in parallel.
  // Same interface as above
  def getDefinitions(
    wordLanguage: Language,
    definitionLanguage: Language,
    words: Seq[String]
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
}
