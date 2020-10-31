package com.foreignlanguagereader.dto.v1.definition

import com.foreignlanguagereader.dto.v1.definition.chinese.ChinesePronunciation
import com.foreignlanguagereader.dto.v1.definition.chinese.HskLevel.HSKLevel
import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO.PartOfSpeechDTO
import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{ObjectTypeDescription, deriveObjectType}
import sangria.schema.ObjectType

case class ChineseDefinitionDTO(
    id: String,
    subdefinitions: List[String],
    tag: PartOfSpeechDTO,
    examples: Option[List[String]],
    simplified: Option[String],
    traditional: Option[Seq[String]],
    pronunciation: ChinesePronunciation,
    hsk: HSKLevel
) extends DefinitionDTO

object ChineseDefinitionDTO {
  implicit val format: Format[ChineseDefinitionDTO] = Json.format

  implicit val graphQlType: ObjectType[Unit, ChineseDefinitionDTO] =
    deriveObjectType[Unit, ChineseDefinitionDTO](
      ObjectTypeDescription(
        "A specialized definition type for words in Chinese"
      )
    )
}
