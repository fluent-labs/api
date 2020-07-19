package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.definition.ChinesePronunciation
import com.foreignlanguagereader.api.domain.definition.HskLevel.HSKLevel
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{ObjectTypeDescription, deriveObjectType}
import sangria.schema.ObjectType

case class ChineseDefinitionDTO(subdefinitions: List[String],
                                tag: Option[PartOfSpeech],
                                examples: Option[List[String]],
                                simplified: Option[String],
                                traditional: Option[String],
                                pronunciation: ChinesePronunciation,
                                hsk: HSKLevel)
    extends DefinitionDTO

object ChineseDefinitionDTO {
  implicit val format: Format[ChineseDefinitionDTO] = Json.format

  implicit val graphQlType: ObjectType[Unit, ChineseDefinitionDTO] =
    deriveObjectType[Unit, ChineseDefinitionDTO](
      ObjectTypeDescription(
        "A specialized definition type for words in Chinese"
      )
    )
}
