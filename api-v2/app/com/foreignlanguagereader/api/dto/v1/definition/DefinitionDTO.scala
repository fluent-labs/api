package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json.{Format, JsError, JsResult, JsValue}
import sangria.schema.UnionType

trait DefinitionDTO {
  val subdefinitions: List[String]
  val tag: Option[PartOfSpeech]
  val examples: List[String]
}
object DefinitionDTO {
  // JSON
  implicit val formatDefinitionDTO: Format[DefinitionDTO] =
    new Format[DefinitionDTO] {
      override def reads(json: JsValue): JsResult[DefinitionDTO] =
        JsError("We are not supposed to read definitions in")
      override def writes(o: DefinitionDTO): JsValue = o match {
        case c: ChineseDefinitionDTO => ChineseDefinitionDTO.format.writes(c)
        case g: GenericDefinitionDTO =>
          GenericDefinitionDTO.format.writes(g)
      }
    }

  // Graphql
  val graphQlType = UnionType(
    "definition",
    Some("A definition for a word"),
    List(ChineseDefinitionDTO.graphQlType, GenericDefinitionDTO.graphQlType)
  )

  // Constructor that defaults to generic
  def apply(subdefinitions: List[String],
            tag: Option[PartOfSpeech],
            examples: List[String]): DefinitionDTO =
    GenericDefinitionDTO(subdefinitions, tag, examples)
}
