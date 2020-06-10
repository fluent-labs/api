package com.foreignlanguagereader.api.domain.definition.entry

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}

/*
 * An enum that defines where a definition came from
 */
object DefinitionSource extends Enumeration {
  type DefinitionSource = Value
  val WIKTIONARY: Value = Value("WIKTIONARY")
  val CEDICT: Value = Value("CEDICT")
  val MULTIPLE: Value = Value("MULTIPLE")

  // Makes sure we can serialize and deserialize this to JSON
  implicit val sourceFormat: Format[DefinitionSource] =
    new Format[DefinitionSource] {
      def reads(json: JsValue) =
        JsSuccess(DefinitionSource.withName(json.as[String]))
      def writes(source: DefinitionSource.DefinitionSource) =
        JsString(source.toString)
    }
}
