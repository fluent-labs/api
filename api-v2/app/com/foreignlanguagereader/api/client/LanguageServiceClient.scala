package com.foreignlanguagereader.api.client

import com.foreignlanguagereader.api.Language.Language
import com.foreignlanguagereader.api.ReadinessStatus
import com.foreignlanguagereader.api.ReadinessStatus.ReadinessStatus
import com.foreignlanguagereader.api.domain.definition.Definition
import com.foreignlanguagereader.api.dto.v1.Word
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class LanguageServiceClient @Inject()(val ws: WSClient,
                                      implicit val ec: ExecutionContext) {
  val languageServiceBaseUrl = "languageServiceBaseUrl"
  val definitionUrl =
    s"$languageServiceBaseUrl/v1/definition/<string:language>/<string:word>"
  val taggingUrl =
    s"$languageServiceBaseUrl/v1/tagging/<string:language>/document"

  def checkConnection: ReadinessStatus = {
    val request = ws.url(s"$languageServiceBaseUrl/health").get()
    val response = Await.result(request, Duration(5, TimeUnit.SECONDS))
    response.status match {
      case OK => ReadinessStatus.UP
      case _  => ReadinessStatus.DOWN
    }
  }

  def getDefinition(wordLanguage: Language,
                    definitionLanguage: Language,
                    word: String): List[Definition] = ???

  def getWordsForDocument(documentLanguage: Language,
                          document: String): List[Word] = ???
}
