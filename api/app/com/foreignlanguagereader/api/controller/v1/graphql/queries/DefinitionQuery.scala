package com.foreignlanguagereader.api.controller.v1.graphql.queries

import com.foreignlanguagereader.dto.v1.definition.DefinitionDTO
import com.foreignlanguagereader.domain.service.definition.DefinitionService
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import javax.inject.Inject
import sangria.schema.{Argument, Field, ListType, ObjectType, StringType}

import scala.concurrent.ExecutionContext

class DefinitionQuery @Inject() (implicit val ec: ExecutionContext) {
  val wordLanguageArgument: Argument[Language] =
    Argument("wordLanguage", Language.graphqlType)
  val definitionLanguageArgument: Argument[Language] =
    Argument("definitionLanguage", Language.graphqlType)
  val tokenArgument: Argument[String] = Argument("token", StringType)

  val field: Field[DefinitionService, Unit] = Field(
    "definition",
    ListType(DefinitionDTO.graphQlType),
    description = Some("Returns definitions for a given word"),
    arguments =
      List(wordLanguageArgument, definitionLanguageArgument, tokenArgument),
    resolve = c => {
      val wordLanguage = c arg wordLanguageArgument
      val definitionLanguage = c arg definitionLanguageArgument
      val token = c arg tokenArgument
      c.ctx
        .getDefinition(
          wordLanguage,
          definitionLanguage,
          Word.fromToken(token, wordLanguage)
        )
        .map(definitions => definitions.map(_.toDTO))
    }
  )

  val query: ObjectType[DefinitionService, Unit] =
    ObjectType("Query", List(field))
}
