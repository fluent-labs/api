package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined
import com.foreignlanguagereader.api.domain.definition.combined.HskLevel.HSKLevel
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  ChinesePronunciation,
  Definition,
  HskLevel
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionEntry,
  DefinitionSource,
  WiktionaryDefinitionEntry
}
import com.foreignlanguagereader.api.util.ContentFileLoader
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{Json, Reads}

import scala.concurrent.ExecutionContext

/**
  * Language specific handling for Chinese.
  * We have two dictionaries here, so we should combine them to produce the best possible results
  * In particular, CEDICT has a minimum level of quality, but doesn't have as many definitions.
  */
class ChineseDefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val logger: Logger = Logger(this.getClass)

  override val wordLanguage: Language = Language.CHINESE
  override val sources: Set[DefinitionSource] =
    Set(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)
  override val webSources: Set[DefinitionSource] = Set(
    DefinitionSource.WIKTIONARY
  )

  override def enrichDefinitions(
    definitionLanguage: Language,
    word: String,
    definitions: Seq[DefinitionEntry]
  ): Seq[Definition] = {
    definitionLanguage match {
      case Language.ENGLISH => enrichEnglishDefinitions(word, definitions)
      case _                => super.enrichDefinitions(definitionLanguage, word, definitions)
    }
  }

  private def enrichEnglishDefinitions(
    word: String,
    definitions: Seq[DefinitionEntry]
  ): Seq[Definition] = {
    val (cedict, wiktionary) = partitionResultsByDictionary(definitions)
    logger.info(
      s"Enhancing results for $word using cedict with ${cedict.size} cedict results and ${wiktionary.size} wiktionary results"
    )

    (cedict, wiktionary) match {
      case (cedict, wiktionary) if wiktionary.isEmpty =>
        logger.info(s"Using cedict definitions for $word")
        cedict.map(_.toDefinition)
      case (cedict, wiktionary) if cedict.isEmpty =>
        logger.info(s"Using wiktionary definitions for $word")
        wiktionary.map(_.toDefinition)
      // If CEDICT doesn't have subdefinitions, then we should return wiktionary data
      // We still want pronunciation and simplified/traditional mapping, so we will add cedict data
      case (cedict, wiktionary) if cedict(0).subdefinitions.isEmpty =>
        logger.info(s"Using enhanced wiktionary definitions for $word")
        addCedictDataToWiktionaryResults(word, cedict(0), wiktionary)
      // If are definitions from CEDICT, they are better.
      // In that case, we only want part of speech tag and examples from wiktionary.
      // But everything else will be the single CEDICT definition
      case (cedict, wiktionary) =>
        logger.info(s"Using enhanced cedict definitions for $word")
        addWiktionaryDataToCedictResults(word, cedict(0), wiktionary)
    }
  }

  private def partitionResultsByDictionary(
    definitions: Seq[DefinitionEntry]
  ): (List[CEDICTDefinitionEntry], List[WiktionaryDefinitionEntry]) = {
    definitions.foldLeft(
      (List[CEDICTDefinitionEntry](), List[WiktionaryDefinitionEntry]())
    )(
      (acc, entry) =>
        entry match {
          case c: CEDICTDefinitionEntry     => (c :: acc._1, acc._2)
          case w: WiktionaryDefinitionEntry => (acc._1, w :: acc._2)
      }
    )
  }

  private def addCedictDataToWiktionaryResults(
    word: String,
    cedict: CEDICTDefinitionEntry,
    wiktionary: Seq[WiktionaryDefinitionEntry]
  ): Seq[ChineseDefinition] = {
    wiktionary.map(
      w =>
        ChineseDefinition(
          w.subdefinitions,
          w.tag,
          w.examples,
          cedict.pinyin,
          cedict.simplified,
          cedict.traditional,
          w.definitionLanguage,
          DefinitionSource.MULTIPLE,
          w.tag
      )
    )
  }

  private def addWiktionaryDataToCedictResults(
    word: String,
    cedict: CEDICTDefinitionEntry,
    wiktionary: Seq[WiktionaryDefinitionEntry]
  ): Seq[ChineseDefinition] = {
    val examples = wiktionary.foldLeft(List[String]())(
      (acc, entry: WiktionaryDefinitionEntry) => {
        acc ++ entry.examples
      }
    )
    Seq(
      ChineseDefinition(
        cedict.subdefinitions,
        wiktionary(0).tag,
        examples,
        cedict.pinyin,
        cedict.simplified,
        cedict.traditional,
        Language.ENGLISH,
        DefinitionSource.MULTIPLE,
        word
      )
    )
  }
}

object ChineseDefinitionService {
  // This tags the words with zhuyin, wade giles, and IPA based on the pinyin.
  // It also pulls the tones out of the pinyin as a separate thing
  // This works because pinyin is a perfect sound system
  def getPronunciation(
    pinyin: String
  ): Tuple2[Option[ChinesePronunciation], Option[Array[String]]] = {
    val (pronunciation, tones) = pinyin
      .split(" ")
      .map {
        case hasTone if hasTone.takeRight(1).matches("[12345]+") =>
          (hasTone.dropRight(1), Some(hasTone.takeRight(1)))
        case noTone => (noTone, None)
      }
      .map {
        case (rawPinyin, tone) => (pronunciations.get(rawPinyin), tone)
      }
      .unzip

    (pronunciation, tones) match {
      case (p, t) if t.forall(_.isDefined) && p.forall(_.isDefined) =>
        (Some(p.flatten.reduce(_ + _)), Some(t.flatten))
      case (p, t) if p.forall(_.isDefined) =>
        (Some(p.flatten.reduce(_ + _)), None)
      case _ => (None, None)
    }
  }

  def getHSK(simplified: String): HSKLevel = hsk.getLevel(simplified)

  private[this] val pronunciations: Map[String, ChinesePronunciation] =
    ContentFileLoader
      .loadJsonResourceFile[Seq[ChinesePronunciation]](
        "/resources/definition/chinese/pronunciation.json"
      )
      .map(pron => pron.pinyin -> pron)
      .toMap

  private[this] val hsk: HskHolder = ContentFileLoader
    .loadJsonResourceFile[HskHolder]("/resources/definition/chinese/hsk.json")
}

case class HskHolder(hsk1: Set[String],
                     hsk2: Set[String],
                     hsk3: Set[String],
                     hsk4: Set[String],
                     hsk5: Set[String],
                     hsk6: Set[String]) {
  def getLevel(simplified: String): combined.HskLevel.Value = simplified match {
    case s if hsk1.contains(s) => HskLevel.ONE
    case s if hsk2.contains(s) => HskLevel.TWO
    case s if hsk3.contains(s) => HskLevel.THREE
    case s if hsk4.contains(s) => HskLevel.FOUR
    case s if hsk5.contains(s) => HskLevel.FIVE
    case s if hsk6.contains(s) => HskLevel.SIX
    case _                     => HskLevel.NONE
  }
}
object HskHolder {
  implicit val reads: Reads[HskHolder] = Json.reads[HskHolder]
}
