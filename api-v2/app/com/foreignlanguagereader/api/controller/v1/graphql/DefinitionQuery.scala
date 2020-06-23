package com.foreignlanguagereader.api.controller.v1.graphql

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.dto.v1.definition.DefinitionDTO
import com.foreignlanguagereader.api.service.definition.DefinitionService
import javax.inject.Inject
import sangria.schema.{Argument, Field, ListType, ObjectType, StringType}

import scala.concurrent.ExecutionContext

class DefinitionQuery @Inject()(implicit val ec: ExecutionContext) {
  val wordLanguage = Argument("wordLanguage", Language.graphqlType)
  val definitionLanguage = Argument("definitionLanguage", Language.graphqlType)
  val token = Argument("token", StringType)

  val field: Field[DefinitionService, Unit] = Field(
    "definition",
    ListType(DefinitionDTO.graphQlType),
    description = Some("Returns definitions for a given word"),
    arguments = List(wordLanguage, definitionLanguage, token),
    resolve = c =>
      c.ctx
        .getDefinition(
          c arg wordLanguage,
          c arg definitionLanguage,
          c arg token
        )
        .map {
          case Some(definitions) => definitions.map(_.toDTO).toList
          case None              => List()
      }
  )

  val query =
    ObjectType("Query", List(field))
}
