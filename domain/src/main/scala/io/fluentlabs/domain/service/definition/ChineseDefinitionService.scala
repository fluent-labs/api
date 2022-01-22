package io.fluentlabs.domain.service.definition

import cats.implicits._
import io.fluentlabs.content.enrichers.chinese.SimplifiedTraditionalConverter
import io.fluentlabs.content.types.Language
import io.fluentlabs.content.types.Language.Language
import io.fluentlabs.content.types.internal.definition
import io.fluentlabs.content.types.internal.definition.DefinitionSource.DefinitionSource
import io.fluentlabs.content.types.internal.definition.{
  ChineseDefinition,
  DefinitionSource
}
import io.fluentlabs.content.types.internal.word.Word
import com.github.houbb.opencc4j.util.ZhConverterUtil
import io.fluentlabs.domain.client.elasticsearch.ElasticsearchCacheClient
import io.fluentlabs.domain.fetcher.DefinitionFetcher
import io.fluentlabs.domain.fetcher.chinese.{
  CEDICTFetcher,
  WiktionaryChineseFetcher
}
import io.fluentlabs.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

/** Language specific handling for Chinese. We have two dictionaries here, so we
  * should combine them to produce the best possible results In particular,
  * CEDICT has a minimum level of quality, but doesn't have as many definitions.
  */
class ChineseDefinitionService @Inject() (
    val elasticsearch: ElasticsearchCacheClient,
    override val config: Configuration,
    val metrics: MetricsReporter,
    cedictFetcher: CEDICTFetcher,
    wiktionaryChineseFetcher: WiktionaryChineseFetcher,
    implicit val ec: ExecutionContext
) extends LanguageDefinitionService[ChineseDefinition] {
  override val logger: Logger = Logger(this.getClass)

  override val wordLanguage: Language = Language.CHINESE
  override val sources: List[DefinitionSource] =
    List(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)

  override val definitionFetchers: Map[
    (DefinitionSource, Language),
    DefinitionFetcher[_, ChineseDefinition]
  ] =
    Map(
      (DefinitionSource.CEDICT, Language.ENGLISH) -> cedictFetcher,
      (
        DefinitionSource.WIKTIONARY,
        Language.ENGLISH
      ) -> wiktionaryChineseFetcher,
      (
        DefinitionSource.WIKTIONARY,
        Language.CHINESE
      ) -> wiktionaryChineseFetcher
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
      definitions: Map[DefinitionSource, List[ChineseDefinition]]
  ): List[ChineseDefinition] = {
    definitionLanguage match {
      case Language.ENGLISH =>
        logger.info("Enriching Chinese definitions in English")
        enrichEnglishDefinitions(word, definitions)
      case _ =>
        logger.info(s"Not enriching Chinese definitions in $definitionLanguage")
        super.enrichDefinitions(definitionLanguage, word, definitions)
    }
  }

  private[this] def enrichEnglishDefinitions(
      word: Word,
      definitions: Map[DefinitionSource, List[ChineseDefinition]]
  ): List[ChineseDefinition] = {
    val cedict = definitions.get(DefinitionSource.CEDICT) match {
      case Some(c) if c.nonEmpty => Some(c)
      case _                     => None
    }
    val wiktionary = definitions.get(DefinitionSource.WIKTIONARY) match {
      case Some(w) if w.nonEmpty => Some(w)
      case _                     => None
    }
    logger.info(
      s"Enhancing results for $word using cedict with ${cedict.size} cedict results and ${wiktionary.size} wiktionary results"
    )

    // TODO - handle CEDICT duplicates

    (cedict, wiktionary) match {
      case (Some(cedict), Some(wiktionary)) =>
        logger.info(s"Combining cedict and wiktionary definitions for $word")
        mergeCedictAndWiktionary(
          word,
          cedict.head,
          wiktionary
        )
      case (Some(cedict), None) =>
        logger.info(s"Using cedict definitions for $word")
        cedict
      case (None, Some(wiktionary)) =>
        logger.info(s"Using wiktionary definitions for $word")
        wiktionary
      // This should not happen. If it does then it's important to log it.
      case (None, None) =>
        val message =
          s"Definitions were lost for chinese word $word, check the request partitioner"
        logger.error(message)
        logger.warn(s"Raw definitions for word $word: $definitions")
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
      definition.ChineseDefinition(
        subdefinitions = w.subdefinitions,
        tag = w.tag,
        examples = w.examples,
        inputPinyin = cedict.pronunciation.pinyin,
        inputSimplified = cedict.simplified,
        inputTraditional = cedict.traditional.map(
          _.head
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
      definition.ChineseDefinition(
        subdefinitions = cedict.subdefinitions,
        tag = wiktionary.head.tag,
        examples = examples,
        inputPinyin = cedict.pronunciation.pinyin,
        inputSimplified = cedict.simplified,
        inputTraditional = cedict.traditional.map(
          _.head
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
