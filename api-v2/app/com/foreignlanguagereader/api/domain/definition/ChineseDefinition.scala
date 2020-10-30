package com.foreignlanguagereader.api.domain.definition

import cats._
import cats.data._
import cats.implicits._

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.HskLevel.HSKLevel
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.api.dto.v1.definition.ChineseDefinitionDTO
import com.foreignlanguagereader.api.service.definition.ChineseDefinitionService
import com.github.houbb.opencc4j.util.ZhConverterUtil
import play.api.libs.json._
import sangria.macros.derive.{
  ObjectTypeDescription,
  deriveEnumType,
  deriveObjectType
}
import sangria.schema.{EnumType, ObjectType}

case class ChineseDefinition(override val subdefinitions: List[String],
                             override val tag: PartOfSpeech,
                             override val examples: Option[List[String]],
                             private val inputPinyin: String = "",
                             private val inputSimplified: Option[String],
                             private val inputTraditional: Option[String],
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             override val definitionLanguage: Language,
                             override val source: DefinitionSource,
                             override val token: String)
    extends Definition {
  private[this] val isTraditional = ZhConverterUtil.isTraditional(token)
  val wordLanguage: Language =
    if (isTraditional) Language.CHINESE_TRADITIONAL else Language.CHINESE

  val pronunciation: ChinesePronunciation =
    ChineseDefinitionService.getPronunciation(inputPinyin)
  val ipa: String = pronunciation.ipa
  val id: String = generateId()

  val (simplified: Option[String], traditional: Option[Seq[String]]) =
    (inputSimplified, inputTraditional) match {
      case (Some(s), Some(t))                => (s.some, List(t).some)
      case (Some(s), None) if isTraditional  => (s.some, List(token).some)
      case (None, Some(t)) if !isTraditional => (token.some, List(t).some)
      case _ =>
        if (isTraditional)
          (ChineseDefinitionService.toSimplified(token), List(token).some)
        else (token.some, ChineseDefinitionService.toTraditional(token))
    }

  val hsk: HSKLevel = simplified match {
    case Some(s) => ChineseDefinitionService.getHSK(s)
    case None    => HskLevel.NONE
  }

  lazy val toDTO: ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      id = id,
      subdefinitions = subdefinitions,
      tag = tag,
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

case class ChinesePronunciation(pinyin: String = "",
                                ipa: String = "",
                                zhuyin: String = "",
                                wadeGiles: String = "",
                                tones: Seq[String] = List()) {
  def +(that: ChinesePronunciation): ChinesePronunciation =
    ChinesePronunciation(
      this.pinyin + " " + that.pinyin,
      this.ipa + " " + that.ipa,
      this.zhuyin + " " + that.zhuyin,
      this.wadeGiles + " " + that.wadeGiles,
      this.tones ++ that.tones
    )
}
object ChinesePronunciation {
  implicit val format: Format[ChinesePronunciation] = Json.format
  implicit val graphQlType: ObjectType[Unit, ChinesePronunciation] =
    deriveObjectType[Unit, ChinesePronunciation](
      ObjectTypeDescription(
        "A holder of different pronunciation formats for Chinese words"
      )
    )
}

object HskLevel extends Enumeration {
  type HSKLevel = Value

  val ONE: Value = Value("1")
  val TWO: Value = Value("2")
  val THREE: Value = Value("3")
  val FOUR: Value = Value("4")
  val FIVE: Value = Value("5")
  val SIX: Value = Value("6")
  val NONE: Value = Value("")

  implicit val hskLevelFormat: Format[HSKLevel] = new Format[HSKLevel] {
    def reads(json: JsValue): JsResult[Value] =
      JsError("We don't read these in, only export")
    def writes(level: HskLevel.HSKLevel) = JsString(level.toString)
  }
  implicit val graphQlType: EnumType[HskLevel.Value] =
    deriveEnumType[HskLevel.Value]()
}
