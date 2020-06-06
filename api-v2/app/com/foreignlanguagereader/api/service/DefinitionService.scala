package com.foreignlanguagereader.api.service

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.definition.{
  ChineseDefinition,
  Definition
}
import com.foreignlanguagereader.api.domain.definition.chinese.CEDICTDefinition
import com.foreignlanguagereader.api.dto.v1.definition.{
  ChineseDefinitionDTO,
  DefinitionDTO,
  GenericDefinitionDTO
}
import com.foreignlanguagereader.api.{DefinitionSource, Language}
import javax.inject.{Inject, Singleton}

@Singleton
class DefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient
) {
  def getDefinition(wordLanguage: Language,
                    definitionLanguage: Language,
                    word: String): List[DefinitionDTO] = wordLanguage match {
    case Language.CHINESE => getChineseDefinition(definitionLanguage, word)
    case Language.ENGLISH => getEnglishDefinition(definitionLanguage, word)
    case Language.SPANISH => getSpanishDefinition(definitionLanguage, word)
  }

  def getDefinitions(wordLanguage: Language,
                     definitionLanguage: Language,
                     words: List[String]): List[DefinitionDTO] =
    words.flatMap(word => getDefinition(wordLanguage, definitionLanguage, word))

  def getChineseDefinition(definitionLanguage: Language,
                           word: String): List[ChineseDefinitionDTO] = {
    val rawDefinitions =
      findOrFetchDefinition(definitionLanguage, Language.CHINESE, word)

    // There is only one CEDICT definition so we should use that.
    val CEDICTDefinition: Option[CEDICTDefinition] = rawDefinitions
      .find(_.source.equals(DefinitionSource.CEDICT))
      .asInstanceOf[Option[CEDICTDefinition]]

    rawDefinitions
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

  // Passthrough for now, but these will contain logic that combines data sources based on what dictionaries we have for the language
  def getEnglishDefinition(definitionLanguage: Language,
                           word: String): List[DefinitionDTO] =
    findOrFetchDefinition(definitionLanguage, Language.ENGLISH, word)

  // Passthrough for now, but these will contain logic that combines data sources based on what dictionaries we have for the language
  def getSpanishDefinition(definitionLanguage: Language,
                           word: String): List[DefinitionDTO] =
    findOrFetchDefinition(definitionLanguage, Language.SPANISH, word)

  // Long term roadmap is to have all language content in elasticsearch.
  // Right now it is not all there, so we should be prepared to scrape wiktionary for anything missing
  // And that functionality is in language service.
  // It's not worth rewriting it for an intermediate step, so we have this hack for now
  def findOrFetchDefinition(wordLanguage: Language,
                            definitionLanguage: Language,
                            word: String): List[Definition] =
    elasticsearch.getDefinition(definitionLanguage, wordLanguage, word) match {
      case Nil =>
        languageServiceClient.getDefinition(
          wordLanguage,
          definitionLanguage,
          word
        )
      case definitions => definitions
    }
}
