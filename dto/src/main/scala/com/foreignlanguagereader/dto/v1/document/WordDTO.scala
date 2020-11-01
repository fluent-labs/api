package com.foreignlanguagereader.dto.v1.document

import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO
import play.api.libs.json.{Format, Json, Reads}

case class WordDTO(
    token: String,
    tag: String,
    lemma: String,
    definitions: Seq[DefinitionDTO]
)

object WordDTO {
  implicit val format: Format[WordDTO] = Json.format
  implicit val reads: Reads[WordDTO] =
    Json.reads[WordDTO]
  implicit val readsSeq: Reads[Seq[WordDTO]] =
    Reads.seq(reads)
}
