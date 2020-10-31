package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.Definition
import com.foreignlanguagereader.domain.internal.word.Word
import javax.inject.{Inject, Singleton}
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefinitionService @Inject() (
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
  def getDefinition(
      wordLanguage: Language,
      definitionLanguage: Language,
      word: Word
  ): Future[List[Definition]] =
    wordLanguage match {
      case Language.CHINESE =>
        chineseDefinitionService.getDefinitions(definitionLanguage, word)
      case Language.ENGLISH =>
        englishDefinitionService.getDefinitions(definitionLanguage, word)
      case Language.SPANISH =>
        spanishDefinitionService.getDefinitions(definitionLanguage, word)
    }
}
