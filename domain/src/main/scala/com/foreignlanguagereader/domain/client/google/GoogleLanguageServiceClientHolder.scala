package com.foreignlanguagereader.domain.client.google

import com.google.cloud.language.v1.{
  AnalyzeSyntaxRequest,
  AnalyzeSyntaxResponse,
  LanguageServiceClient,
  Token
}
import play.api.Logger

import javax.inject.Singleton
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

// $COVERAGE-OFF$
/** Holder for the google cloud client.
  * Allows us to swap this out for testing
  */
@Singleton
class GoogleLanguageServiceClientHolder {
  val logger: Logger = Logger(this.getClass)
  private[this] val gcloud: Option[LanguageServiceClient] = Try(
    LanguageServiceClient.create()
  ) match {
    case Success(value) => Some(value)
    case Failure(e) =>
      logger.error("Failed to provision google cloud client", e)
      None
  }

  def analyzeSyntax(request: AnalyzeSyntaxRequest): AnalyzeSyntaxResponse = {
    if (gcloud.isDefined) {
      gcloud.get.analyzeSyntax(request)
    } else {
      throw new IllegalStateException("Google cloud client was not provisioned")
    }
  }

  def getTokens(request: AnalyzeSyntaxRequest): List[Token] =
    analyzeSyntax(request).getTokensList.asScala.toList
}
// $COVERAGE-ON$
