package com.foreignlanguagereader.dto.v1.definition

import com.foreignlanguagereader.dto.v1.word.PartOfSpeechDTO.PartOfSpeechDTO
import play.api.libs.json.{Format, JsError, JsResult, JsValue}
import sangria.schema.UnionType

trait DefinitionDTO {
  val id: String
  val subdefinitions: List[String]
  val tag: PartOfSpeechDTO
  val examples: Option[List[String]]
}
object DefinitionDTO {
  // JSON
  implicit val formatDefinitionDTO: Format[DefinitionDTO] =
    new Format[DefinitionDTO] {
      override def reads(json: JsValue): JsResult[DefinitionDTO] =
        JsError("We are not supposed to read definitions in")
      override def writes(o: DefinitionDTO): JsValue =
        o match {
          case c: ChineseDefinitionDTO => ChineseDefinitionDTO.format.writes(c)
          case g: GenericDefinitionDTO =>
            GenericDefinitionDTO.format.writes(g)
        }
    }

  // Graphql
  val graphQlType: UnionType[Unit] = UnionType(
    "definition",
    Some("A definition for a word"),
    List(ChineseDefinitionDTO.graphQlType, GenericDefinitionDTO.graphQlType)
  )

  // Constructor that defaults to generic
  def apply(
      id: String,
      subdefinitions: List[String],
      tag: PartOfSpeechDTO,
      examples: Option[List[String]]
  ): DefinitionDTO =
    GenericDefinitionDTO(
      id = id,
      subdefinitions = subdefinitions,
      tag = tag,
      examples = examples
    )
}
