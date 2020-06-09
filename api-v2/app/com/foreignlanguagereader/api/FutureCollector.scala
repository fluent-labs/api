package com.foreignlanguagereader.api

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class FutureCollector @Inject()(implicit val ec: ExecutionContext) {
  def collectFuturesIntoSingleResult[K, V](
    inputs: Map[K, Future[V]]
  ): Future[Map[K, V]] = {
    inputs.foldLeft(Future.successful(Map.empty[K, V])) {
      case (futureAcc: Future[Map[K, V]], (k: K, futureV: Future[V])) =>
        for {
          acc <- futureAcc
          v <- futureV
        } yield acc + (k -> v)
    }
  }
}
