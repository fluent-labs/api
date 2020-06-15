package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.definition.combined.ChinesePronunciation
import com.foreignlanguagereader.api.domain.definition.combined.HskLevel.HSKLevel
import play.api.libs.json.{Format, Json}

case class ChineseDefinitionDTO(subdefinitions: List[String],
                                tag: String,
                                examples: List[String],
                                simplified: String = "",
                                traditional: String = "",
                                pronunciation: ChinesePronunciation,
                                hsk: HSKLevel)
    extends DefinitionDTO

object ChineseDefinitionDTO {
  implicit val format: Format[ChineseDefinitionDTO] = Json.format
}
