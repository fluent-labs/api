package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.definition.combined.HSKLevel.HSKLevel
import play.api.libs.json.{Format, Json}

case class ChineseDefinitionDTO(subdefinitions: List[String],
                                tag: String,
                                examples: List[String],
                                pinyin: String = "",
                                simplified: String = "",
                                traditional: String = "",
                                ipa: String,
                                zhuyin: String,
                                wadeGiles: String,
                                hsk: HSKLevel)
    extends DefinitionDTO

object ChineseDefinitionDTO {
  implicit val format: Format[ChineseDefinitionDTO] = Json.format
}
