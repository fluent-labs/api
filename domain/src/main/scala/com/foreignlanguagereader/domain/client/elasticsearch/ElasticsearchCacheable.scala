package com.foreignlanguagereader.domain.client.elasticsearch

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Reads, Writes}

case class ElasticsearchCacheable[T](item: T, fields: Map[String, String])
object ElasticsearchCacheable {
  implicit def reads[T](implicit
      r: Reads[T]
  ): Reads[ElasticsearchCacheable[T]] =
    (
      (JsPath \ "item").read(r) and
        (JsPath \ "fields").read[Map[String, String]]
    )(ElasticsearchCacheable.apply[T] _)

  implicit def writes[T](implicit
      w: Writes[T]
  ): Writes[ElasticsearchCacheable[T]] =
    (
      (JsPath \ "item").write(w) and
        (JsPath \ "fields").write[Map[String, String]]
    )(unlift(ElasticsearchCacheable.unapply[T]))
}
