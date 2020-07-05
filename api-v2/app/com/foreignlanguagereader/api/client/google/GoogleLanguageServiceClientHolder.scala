package com.foreignlanguagereader.api.client.google

import com.google.cloud.language.v1.{
  AnalyzeSyntaxRequest,
  AnalyzeSyntaxResponse,
  LanguageServiceClient,
  Token
}
import scala.jdk.CollectionConverters._

/**
  * Holder for the google cloud client.
  * Allows us to swap this out for testing
  */
class GoogleLanguageServiceClientHolder {
  private[this] val gcloud: LanguageServiceClient =
    LanguageServiceClient.create()

  def analyzeSyntax(request: AnalyzeSyntaxRequest): AnalyzeSyntaxResponse =
    gcloud.analyzeSyntax(request)

  def getTokens(request: AnalyzeSyntaxRequest): List[Token] =
    analyzeSyntax(request).getTokensList.asScala.toList
}
