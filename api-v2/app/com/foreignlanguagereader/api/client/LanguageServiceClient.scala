package com.foreignlanguagereader.api.client

import java.util.concurrent.TimeUnit

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.ReadinessStatus
import com.foreignlanguagereader.api.ReadinessStatus.ReadinessStatus
import com.foreignlanguagereader.api.domain.definition.combined.Definition
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionEntry,
  WiktionaryDefinitionEntry
}
import com.foreignlanguagereader.api.dto.v1.Word
import javax.inject.Inject
import play.api.http.Status.OK
import play.api.libs.json.{JsSuccess, Json, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class LanguageServiceClient @Inject()(val ws: WSClient,
                                      implicit val ec: ExecutionContext) {
  val languageServiceBaseUrl = "languageServiceBaseUrl"
  val languageServiceTimeout = Duration(5, TimeUnit.SECONDS)

  // Tells the JSON library that it can handle these types
  implicit val cedictDefinitionReads: Reads[CEDICTDefinitionEntry] =
    Json.reads[CEDICTDefinitionEntry]
  implicit val cedictDefinitionListReads: Reads[Seq[CEDICTDefinitionEntry]] =
    Reads.seq(cedictDefinitionReads)
  implicit val wiktionaryDefinitionReads: Reads[WiktionaryDefinitionEntry] =
    Json.reads[WiktionaryDefinitionEntry]
  implicit val wiktionaryDefinitionListReads
    : Reads[Seq[WiktionaryDefinitionEntry]] =
    Reads.seq(wiktionaryDefinitionReads)
  implicit val documentReads: Reads[Word] = Json.reads[Word]
  implicit val documentListReads: Reads[Seq[Word]] =
    Reads.seq(documentReads)

  // Used for the health check. Just makes sure we can connect to language service
  def checkConnection: ReadinessStatus = {
    val request = ws.url(s"$languageServiceBaseUrl/health").get()
    Await.result(request, languageServiceTimeout).status match {
      case OK => ReadinessStatus.UP
      case _  => ReadinessStatus.DOWN
    }
  }

  def getDefinition(wordLanguage: Language,
                    _definitionLanguage: Language,
                    word: String): Option[Seq[DefinitionEntry]] = {
    val request =
      ws.url(s"$languageServiceBaseUrl/v1/definition/$language/$word").get()
    Await
      .result(request, languageServiceTimeout)
      .json
      .validate[Seq[Word]] match {
      case d: JsSuccess[Seq[DefinitionEntry]] => Some(d.get)
      case _                                  => None
    }
  }

  def getWordsForDocument(documentLanguage: Language,
                          document: String): Option[Seq[Word]] = {
    val request = ws
      .url(s"$languageServiceBaseUrl/v1/tagging/$documentLanguage/document")
      .post(document)
    Await
      .result(request, languageServiceTimeout)
      .json
      .validate(documentListReads) match {
      case w: JsSuccess[Seq[Word]] => Some(w.get)
      case _                       => None
    }
  }
}
