package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.HSKLevel.HSKLevel
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.ChineseDefinitionDTO
import play.api.libs.json._

case class ChineseDefinition(override val subdefinitions: List[String],
                             override val tag: String,
                             override val examples: List[String],
                             pinyin: String = "",
                             simplified: String = "",
                             traditional: String = "",
                             hsk: HSKLevel = HSKLevel.NONE,
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             override val source: DefinitionSource =
                               DefinitionSource.MULTIPLE,
                             override val token: String = "")
    extends Definition {
  val language: Language = Language.CHINESE
  lazy val toDTO: ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      subdefinitions,
      tag,
      examples,
      pinyin,
      simplified,
      traditional,
      hsk
    )
}

object HSKLevel extends Enumeration {
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
      JsSuccess(HSKLevel.withName(json.as[String]))
    def writes(level: HSKLevel.HSKLevel) = JsString(level.toString)
  }
}
