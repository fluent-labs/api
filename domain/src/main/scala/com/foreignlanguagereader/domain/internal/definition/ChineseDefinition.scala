package com.foreignlanguagereader.domain.internal.definition

import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.content.chinese.{
  ChinesePronunciationGenerator,
  HSKLevelFinder,
  SimplifiedTraditionalConverter
}
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech
import com.foreignlanguagereader.domain.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.dto.v1.definition.ChineseDefinitionDTO
import com.foreignlanguagereader.dto.v1.definition.chinese.HskLevel.HSKLevel
import com.foreignlanguagereader.dto.v1.definition.chinese.{
  ChinesePronunciation,
  HskLevel
}
import com.github.houbb.opencc4j.util.ZhConverterUtil
import play.api.libs.json._

case class ChineseDefinition(
    override val subdefinitions: List[String],
    override val tag: PartOfSpeech,
    override val examples: Option[List[String]],
    private val inputPinyin: String = "",
    private val inputSimplified: Option[String],
    private val inputTraditional: Option[String],
    // These fields are needed for elasticsearch lookup
    // But do not need to be presented to the user.
    override val definitionLanguage: Language,
    override val source: DefinitionSource,
    override val token: String
) extends Definition {
  private[this] val isTraditional = ZhConverterUtil.isTraditional(token)
  val wordLanguage: Language =
    if (isTraditional) Language.CHINESE_TRADITIONAL else Language.CHINESE

  val pronunciation: ChinesePronunciation =
    ChinesePronunciationGenerator.getPronunciation(inputPinyin)
  val ipa: String = pronunciation.ipa
  val id: String = generateId()

  val (simplified: Option[String], traditional: Option[Seq[String]]) =
    (inputSimplified, inputTraditional) match {
      case (Some(s), Some(t))                => (Some(s), Some(List(t)))
      case (Some(s), None) if isTraditional  => (Some(s), Some(List(token)))
      case (None, Some(t)) if !isTraditional => (Some(token), Some(List(t)))
      case _ =>
        if (isTraditional)
          (
            SimplifiedTraditionalConverter.toSimplified(token),
            Some(List(token))
          )
        else (Some(token), SimplifiedTraditionalConverter.toTraditional(token))
    }

  val hsk: HSKLevel = simplified match {
    case Some(s) => HSKLevelFinder.getHSK(s)
    case None    => HskLevel.NONE
  }

  lazy val toDTO: ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      id = id,
      subdefinitions = subdefinitions,
      tag = PartOfSpeech.toDTO(tag),
      examples = examples,
      simplified = simplified,
      traditional = traditional,
      pronunciation = pronunciation,
      hsk = hsk
    )
}
object ChineseDefinition {
  implicit val format: Format[ChineseDefinition] =
    Json.format[ChineseDefinition]
}
