package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.HSKLevel.HSKLevel
import com.foreignlanguagereader.api.domain.definition.ChineseDefinition
import play.api.libs.json.{Format, Json}

case class ChineseDefinitionDTO(subdefinitions: List[String],
                                tag: String,
                                examples: List[String],
                                pinyin: String = "",
                                simplified: String = "",
                                traditional: String = "",
                                hsk: HSKLevel)
    extends DefinitionDTO(subdefinitions, tag, examples)

object ChineseDefinitionDTO {
  implicit def chineseDefinitionToDefinitionDTO(
    definition: ChineseDefinition
  ): ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      definition.subdefinitions,
      definition.tag,
      definition.examples,
      definition.pinyin,
      definition.simplified,
      definition.traditional,
      definition.hsk
    )

  implicit def definitionListToDefinitionDTOList(
    definitions: List[ChineseDefinition]
  ): List[ChineseDefinitionDTO] =
    definitions.map(x => chineseDefinitionToDefinitionDTO(x))

  implicit val format: Format[ChineseDefinitionDTO] = Json.format
}
