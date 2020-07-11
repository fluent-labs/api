package com.foreignlanguagereader.api.domain.definition

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.HskLevel.HSKLevel
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.api.dto.v1.definition.ChineseDefinitionDTO
import com.foreignlanguagereader.api.service.definition.ChineseDefinitionService
import play.api.libs.json._
import sangria.macros.derive.{
  ObjectTypeDescription,
  deriveEnumType,
  deriveObjectType
}
import sangria.schema.{EnumType, ObjectType}

case class ChineseDefinition(override val subdefinitions: List[String],
                             override val tag: Option[PartOfSpeech],
                             override val examples: List[String],
                             private val inputPinyin: String = "",
                             simplified: String = "",
                             traditional: String = "",
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             override val definitionLanguage: Language,
                             override val source: DefinitionSource,
                             override val token: String)
    extends Definition {
  val wordLanguage: Language = Language.CHINESE

  val pronunciation: ChinesePronunciation =
    ChineseDefinitionService.getPronunciation(inputPinyin)

  val hsk: HSKLevel = ChineseDefinitionService.getHSK(simplified)

  lazy val toDTO: ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      subdefinitions,
      tag,
      examples,
      simplified,
      traditional,
      pronunciation,
      hsk
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
