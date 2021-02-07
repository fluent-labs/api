package com.foreignlanguagereader.content.types.internal.definition

import com.foreignlanguagereader.content.difficulty.chinese.hsk.HSKDifficultyFinder
import com.foreignlanguagereader.content.enrichers.chinese.{
  ChinesePronunciationGenerator,
  SimplifiedTraditionalConverter
}
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.dto.v1.definition.{
  ChineseDefinitionDTO,
  DefinitionSourceDTO
}
import com.foreignlanguagereader.dto.v1.definition.chinese.{
  ChinesePronunciation,
  HSKLevel
}
import com.github.houbb.opencc4j.util.ZhConverterUtil
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

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
  override val wordLanguage: Language =
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
    case Some(s) => HSKDifficultyFinder.getDifficulty(s)
    case None    => HSKLevel.NONE
  }

  lazy val toDTO: ChineseDefinitionDTO =
    new ChineseDefinitionDTO(
      id,
      subdefinitions.asJava,
      PartOfSpeech.toDTO(tag),
      DefinitionSource.toDTO(source),
      examples.getOrElse(List()).asJava,
      simplified.asJava,
      traditional.map(_.asJava).asJava,
      pronunciation.pinyin,
      hsk
    )
}
object ChineseDefinition {
  implicit val writes: Writes[ChineseDefinition] = {
    (Json.writes[ChineseDefinition] ~ (__ \ "wordLanguage")
      .write[Language] ~ (__ \ "ipa").write[String])((d: ChineseDefinition) =>
      (d, Language.CHINESE, d.ipa)
    )

  }
  implicit val reads: Reads[ChineseDefinition] = Json.reads[ChineseDefinition]
}
