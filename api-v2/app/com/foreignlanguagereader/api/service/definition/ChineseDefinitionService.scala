package com.foreignlanguagereader.api.service.definition

import cats.data.Nested
import cats.implicits._
import com.foreignlanguagereader.api.client.LanguageServiceClient
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.repository.definition.Cedict
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.content.chinese.SimplifiedTraditionalConverter
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.internal.definition.{
  ChineseDefinition,
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.domain.internal.word.Word
import com.github.houbb.opencc4j.util.ZhConverterUtil
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
  * Language specific handling for Chinese.
  * We have two dictionaries here, so we should combine them to produce the best possible results
  * In particular, CEDICT has a minimum level of quality, but doesn't have as many definitions.
  */
class ChineseDefinitionService @Inject() (
    val elasticsearch: ElasticsearchClient,
    val languageServiceClient: LanguageServiceClient,
    implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val logger: Logger = Logger(this.getClass)

  override val wordLanguage: Language = Language.CHINESE
  override val sources: List[DefinitionSource] =
    List(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)

  def cedictFetcher: (
      Language,
      Word
  ) => Nested[Future, CircuitBreakerResult, List[Definition]] =
    (_, word: Word) =>
      Cedict.getDefinition(word) match {
        case Some(entries) =>
          Nested(
            CircuitBreakerAttempt(entries.map(_.toDefinition(word.tag)))
              .pure[Future]
          )
        case None => Nested(CircuitBreakerNonAttempt().pure[Future])
      }

  override val definitionFetchers: Map[
    (DefinitionSource, Language),
    (Language, Word) => Nested[Future, CircuitBreakerResult, List[Definition]]
  ] =
    Map(
      (DefinitionSource.CEDICT, Language.ENGLISH) -> cedictFetcher,
      (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
    )

  // Convert everything to traditional
  // We need one lookup token for elasticsearch.
  // And traditional is more specific
  override def preprocessWordForRequest(word: Word): List[Word] =
    if (ZhConverterUtil.isSimple(word.token)) {
      SimplifiedTraditionalConverter.toTraditional(word.token) match {
        case Some(s) =>
          s.map(simplified => word.copy(processedToken = simplified))
        case None => List(word)
      }
    } else List(word)

  override def enrichDefinitions(
      definitionLanguage: Language,
      word: Word,
      definitions: Map[DefinitionSource, List[Definition]]
  ): List[Definition] = {
    definitionLanguage match {
      case Language.ENGLISH => enrichEnglishDefinitions(word, definitions)
      case _                => super.enrichDefinitions(definitionLanguage, word, definitions)
    }
  }

  private[this] def enrichEnglishDefinitions(
      word: Word,
      definitions: Map[DefinitionSource, List[Definition]]
  ): List[Definition] = {
    val cedict = definitions.get(DefinitionSource.CEDICT)
    val wiktionary = definitions.get(DefinitionSource.WIKTIONARY)
    logger.info(
      s"Enhancing results for $word using cedict with ${cedict.size} cedict results and ${wiktionary.size} wiktionary results"
    )

    // TODO - handle CEDICT duplicates

    (cedict, wiktionary) match {
      case (Some(cedict), Some(wiktionary)) =>
        logger.info(s"Combining cedict and wiktionary definitions for $word")
        mergeCedictAndWiktionary(
          word,
          cedict(0).asInstanceOf[ChineseDefinition],
          wiktionary.map(_.asInstanceOf[ChineseDefinition])
        )
      case (Some(cedict), None) =>
        logger.info(s"Using cedict definitions for $word")
        cedict
      case (None, Some(wiktionary)) if cedict.isEmpty =>
        logger.info(s"Using wiktionary definitions for $word")
        wiktionary
      // This should not happen. If it does then it's important to log it.
      case (None, None) =>
        val message =
          s"Definitions were lost for chinese word $word, check the request partitioner"
        logger.error(message)
        throw new IllegalStateException(message)
    }
  }

  private[this] def mergeCedictAndWiktionary(
      word: Word,
      cedict: ChineseDefinition,
      wiktionary: List[ChineseDefinition]
  ): List[ChineseDefinition] = {
    cedict match {
      case empty if empty.subdefinitions.isEmpty =>
        // If CEDICT doesn't have subdefinitions, then we should return wiktionary data
        // We still want pronunciation and simplified/traditional mapping, so we will add cedict data
        addCedictDataToWiktionaryResults(word, cedict, wiktionary)
      // If are definitions from CEDICT, they are better.
      // In that case, we only want part of speech tag and examples from wiktionary.
      // But everything else will be the single CEDICT definition
      case _ => addWiktionaryDataToCedictResults(word, cedict, wiktionary)
    }
  }

  private[this] def addCedictDataToWiktionaryResults(
      word: Word,
      cedict: ChineseDefinition,
      wiktionary: List[ChineseDefinition]
  ): List[ChineseDefinition] = {
    wiktionary.map(w =>
      ChineseDefinition(
        subdefinitions = w.subdefinitions,
        tag = w.tag,
        examples = w.examples,
        inputPinyin = cedict.pronunciation.pinyin,
        inputSimplified = cedict.simplified,
        inputTraditional = cedict.traditional.map(
          _(0)
        ), // CEDICT has only one traditional option
        definitionLanguage = Language.ENGLISH,
        source = DefinitionSource.MULTIPLE,
        token = word.processedToken
      )
    )
  }

  private[this] def addWiktionaryDataToCedictResults(
      word: Word,
      cedict: ChineseDefinition,
      wiktionary: List[ChineseDefinition]
  ): List[ChineseDefinition] = {
    val examples = {
      val e = wiktionary.flatMap(_.examples).flatten
      if (e.isEmpty) None else Some(e)
    }

    List(
      ChineseDefinition(
        subdefinitions = cedict.subdefinitions,
        tag = wiktionary(0).tag,
        examples = examples,
        inputPinyin = cedict.pronunciation.pinyin,
        inputSimplified = cedict.simplified,
        inputTraditional = cedict.traditional.map(
          _(0)
        ), // CEDICT has only one traditional option
        definitionLanguage = Language.ENGLISH,
        source = DefinitionSource.MULTIPLE,
        token = word.processedToken
      )
    )
  }
}

object ChineseDefinitionService {
  def sentenceIsTraditional(sentence: String): Boolean =
    ZhConverterUtil.isTraditional(sentence)
}
