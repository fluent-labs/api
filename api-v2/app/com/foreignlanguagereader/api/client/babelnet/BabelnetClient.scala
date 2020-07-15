package com.foreignlanguagereader.api.client.babelnet

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.common.{
  CircuitBreakerAttempt,
  CircuitBreakerNonAttempt,
  CircuitBreakerResult,
  Circuitbreaker
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import it.uniroma1.lcl.babelnet.{BabelNetQuery, BabelSense}
import it.uniroma1.lcl.jlt.util.{Language => BabelLanguage}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class BabelnetClient @Inject()(babelnet: BabelnetClientHolder,
                               implicit val ec: ExecutionContext,
                               val system: ActorSystem)
    extends Circuitbreaker {
  override val logger: Logger = Logger(this.getClass)

  def getSenses(
    language: Language,
    lemma: String
  ): Future[CircuitBreakerResult[Option[List[BabelSense]]]] = {
    val lang = language match {
      case Language.CHINESE             => BabelLanguage.ZH
      case Language.CHINESE_TRADITIONAL => BabelLanguage.ZH
      case Language.ENGLISH             => BabelLanguage.EN
      case Language.SPANISH             => BabelLanguage.ES
    }

    val query = new BabelNetQuery.Builder(lemma)
      .from(lang)
      .build

    withBreaker({
      Future {
        babelnet.getSenses(query)
      }
    }, _ => true) map {
      case Success(CircuitBreakerAttempt(List())) =>
        CircuitBreakerAttempt(None)
      case Success(CircuitBreakerAttempt(senses)) =>
        CircuitBreakerAttempt(Some(senses))
      case Success(CircuitBreakerNonAttempt()) => CircuitBreakerNonAttempt()
      case Failure(e) =>
        logger.error(
          s"Failed to get senses from babelnet in $language for word $lemma",
          e
        )
        CircuitBreakerAttempt(None)
    }
  }
}
