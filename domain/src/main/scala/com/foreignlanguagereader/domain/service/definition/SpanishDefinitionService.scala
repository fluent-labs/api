package com.foreignlanguagereader.domain.service.definition

import cats.data.Nested
import com.foreignlanguagereader.domain.client.common.{
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.domain.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.domain.client.{
  LanguageServiceClient,
  MirriamWebsterClient
}
import com.foreignlanguagereader.content.types.Language
import com.foreignlanguagereader.content.types.internal.word.Word
import Language.Language
import com.foreignlanguagereader.content.types.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class SpanishDefinitionService @Inject() (
    val elasticsearch: ElasticsearchClient,
    val languageServiceClient: LanguageServiceClient,
    val websterClient: MirriamWebsterClient,
    implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val wordLanguage: Language = Language.SPANISH
  override val sources: List[DefinitionSource] =
    List(DefinitionSource.MIRRIAM_WEBSTER_SPANISH, DefinitionSource.WIKTIONARY)

  def websterFetcher: (
      Language,
      Word
  ) => Nested[Future, CircuitBreakerResult, List[Definition]] =
    (language: Language, word: Word) =>
      language match {
        case Language.ENGLISH =>
          Nested(websterClient.getSpanishDefinition(word).value)
        case _ => Nested(Future.successful(CircuitBreakerNonAttempt()))
      }

  override val definitionFetchers: Map[
    (DefinitionSource, Language),
    (Language, Word) => Nested[Future, CircuitBreakerResult, List[Definition]]
  ] = Map(
    (
      DefinitionSource.MIRRIAM_WEBSTER_SPANISH,
      Language.ENGLISH
    ) -> websterFetcher,
    (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
  )

}