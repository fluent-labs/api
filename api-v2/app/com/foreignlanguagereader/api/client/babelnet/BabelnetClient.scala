package com.foreignlanguagereader.api.client.babelnet

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import it.uniroma1.lcl.babelnet.{BabelNetQuery, BabelSense}
import it.uniroma1.lcl.jlt.util.{Language => BabelLanguage}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class BabelnetClient @Inject()(babelnet: BabelnetClientHolder,
                               implicit val ec: ExecutionContext) {
  val logger: Logger = Logger(this.getClass)

  def getSenses(language: Language,
                lemma: String): Future[Option[List[BabelSense]]] = {
    val lang = language match {
      case Language.CHINESE             => BabelLanguage.ZH
      case Language.CHINESE_TRADITIONAL => BabelLanguage.ZH
      case Language.ENGLISH             => BabelLanguage.EN
      case Language.SPANISH             => BabelLanguage.ES
    }

    val query = new BabelNetQuery.Builder(lemma)
      .from(lang)
      .build

    Future {
      Try(babelnet.getSenses(query)) match {
        case Success(t) =>
          t match {
            case List() =>
              logger.info(
                s"No senses found in language=$language for lemma=$lemma"
              )
              None
            case senses =>
              logger.info(
                s"Found senses in language=$language for lemma=$lemma"
              )
              Some(senses)
          }
        case Failure(e) =>
          logger.error(
            s"Failed to get senses from babelnet: ${e.getMessage}",
            e
          )
          None
      }
    }
  }

//  def getSense() = {
//    getSenses(Language.ENGLISH, "test") map {
//      case Some(senses) => senses.map(sense => sense.)
//    }
//  }
}
