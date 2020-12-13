package com.foreignlanguagereader.dto.v1.serializers

import com.foreignlanguagereader.dto.v1.definition.chinese.HSKLevel
import com.foreignlanguagereader.dto.v1.definition.{
  ChineseDefinitionDTO,
  DefinitionDTO
}
import com.foreignlanguagereader.dto.v1.document.DocumentRequest
import com.foreignlanguagereader.dto.v1.word.{PartOfSpeechDTO, WordDTO}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

object Serializers {
  implicit val documentRequestReader: Reads[DocumentRequest] =
    (JsPath \ "text").read[String].map(text => new DocumentRequest(text))

  implicit val partOfSpeechWrites: Writes[PartOfSpeechDTO] =
    (o: PartOfSpeechDTO) => JsString(o.toString)

  implicit val definitionWrites: Writes[DefinitionDTO] =
    ((JsPath \ "id").write[String] and
      (JsPath \ "subdefinitions").write[Seq[String]] and
      (JsPath \ "tag").write[PartOfSpeechDTO] and
      (JsPath \ "examples").write[Seq[String]])((definition: DefinitionDTO) =>
      (
        definition.getId,
        definition.getSubdefinitions.asScala,
        definition.getTag,
        definition.getExamples.asScala
      )
    )

  implicit val hskWrites: Writes[HSKLevel] =
    (o: HSKLevel) => JsString(o.toString)

  implicit val chineseDefinitionWrites: Writes[ChineseDefinitionDTO] = {
    ((JsPath \ "id").write[String] and
      (JsPath \ "subdefinitions").write[Seq[String]] and
      (JsPath \ "tag").write[PartOfSpeechDTO] and
      (JsPath \ "examples").write[Seq[String]] and
      (JsPath \ "simplified").writeNullable[String] and
      (JsPath \ "traditional").writeNullable[Seq[String]] and
      (JsPath \ "pronunciation").write[String] and
      (JsPath \ "hsk").write[HSKLevel])((definition: ChineseDefinitionDTO) =>
      (
        definition.getId,
        definition.getSubdefinitions.asScala,
        definition.getTag,
        definition.getExamples.asScala,
        definition.getSimplified.asScala,
        definition.getTraditional.asScala.map(_.asScala),
        definition.getPronunciation,
        definition.getHsk
      )
    )
  }

  implicit val wordFormat: Format[WordDTO] = ???
}
