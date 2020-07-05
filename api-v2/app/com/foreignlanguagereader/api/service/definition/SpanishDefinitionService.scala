package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient,
  MirriamWebsterClient
}
import com.foreignlanguagereader.api.contentsource.definition.DefinitionEntry
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class SpanishDefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  val websterClient: MirriamWebsterClient,
  implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val wordLanguage: Language = Language.SPANISH
  override val sources: Set[DefinitionSource] =
    Set(DefinitionSource.MIRRIAM_WEBSTER_SPANISH, DefinitionSource.WIKTIONARY)
  override val fetchableSources: Set[DefinitionSource] =
    Set(DefinitionSource.MIRRIAM_WEBSTER_SPANISH, DefinitionSource.WIKTIONARY)

  def websterFetcher
    : (Language, String) => Future[Option[Seq[DefinitionEntry]]] =
    (language: Language, word: String) =>
      language match {
        case Language.ENGLISH => websterClient.getSpanishDefinition(word)
        case _                => Future.successful(None)
    }

  override val definitionFetchers
    : Map[(DefinitionSource, Language), (Language, String) => Future[
      Option[Seq[DefinitionEntry]]
    ]] = Map(
    (DefinitionSource.MIRRIAM_WEBSTER_SPANISH, Language.ENGLISH) -> websterFetcher,
    (DefinitionSource.WIKTIONARY, Language.ENGLISH) -> languageServiceFetcher
  )

}
