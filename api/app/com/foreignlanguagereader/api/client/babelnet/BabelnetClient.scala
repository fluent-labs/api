package com.foreignlanguagereader.api.client.babelnet

import akka.actor.ActorSystem
import cats.data.Nested
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.domain.Language
import com.foreignlanguagereader.domain.Language.Language
import it.uniroma1.lcl.babelnet.{BabelNetQuery, BabelSense}
import it.uniroma1.lcl.jlt.util.{Language => BabelLanguage}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class BabelnetClient @Inject() (
    babelnet: BabelnetClientHolder,
    implicit val ec: ExecutionContext,
    val system: ActorSystem
) extends Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)

  def getSenses(
      language: Language,
      lemma: String
  ): Nested[Future, CircuitBreakerResult, List[BabelSense]] = {
    val lang = language match {
      case Language.CHINESE             => BabelLanguage.ZH
      case Language.CHINESE_TRADITIONAL => BabelLanguage.ZH
      case Language.ENGLISH             => BabelLanguage.EN
      case Language.SPANISH             => BabelLanguage.ES
    }

    val query = new BabelNetQuery.Builder(lemma)
      .from(lang)
      .build

    withBreaker(
      s"Failed to get senses for language: $language, lemma: $lemma",
      Future {
        babelnet.getSenses(query)
      }
    )
  }
}
