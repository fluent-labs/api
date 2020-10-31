package com.foreignlanguagereader.dto.v1.document

import play.api.libs.json.{Json, Reads}

case class DocumentRequest(text: String)
object DocumentRequest {
  implicit val reads: Reads[DocumentRequest] = Json.reads[DocumentRequest]
}
