package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerNonAttempt,
  CircuitBreakerResult
}
import com.foreignlanguagereader.api.client.elasticsearch.ElasticsearchClient
import com.foreignlanguagereader.api.client.{
  LanguageServiceClient,
  MirriamWebsterClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.{
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.word.Word
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class SpanishDefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  val websterClient: MirriamWebsterClient,
  implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val wordLanguage: Language = Language.SPANISH
  override val sources: List[DefinitionSource] =
    List(DefinitionSource.MIRRIAM_WEBSTER_SPANISH, DefinitionSource.WIKTIONARY)

  def websterFetcher
    : (Language, Word) => Future[CircuitBreakerResult[List[Definition]]] =
    (language: Language, word: Word) =>
      language match {
        case Language.ENGLISH =>
          websterClient.getSpanishDefinition(word)
        case _ => Future.successful(CircuitBreakerNonAttempt())
    }

  override val definitionFetchers
    : Map[(DefinitionSource, Language), (Language, Word) => Future[
      CircuitBreakerResult[List[Definition]]
    ]] = Map(
    (DefinitionSource.MIRRIAM_WEBSTER_SPANISH, Language.ENGLISH) -> websterFetcher,
    (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
  )

}
