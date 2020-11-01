package com.foreignlanguagereader.api.service.definition

import cats.data.Nested
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.{
  LanguageServiceClient,
  MirriamWebsterClient
}
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.internal.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.domain.internal.word.Word
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
