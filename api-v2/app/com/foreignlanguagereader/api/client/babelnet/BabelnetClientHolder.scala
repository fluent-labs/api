package com.foreignlanguagereader.api.client.babelnet

import it.uniroma1.lcl.babelnet.{BabelNet, BabelNetQuery, BabelSense}
import javax.inject.Singleton

import scala.collection.JavaConverters._

/**
  * Holder for the babelnet.
  * Allows us to swap this out for testing
  */
@Singleton
class BabelnetClientHolder {
  private[this] val babelnet: BabelNet = BabelNet.getInstance

  def getSenses(query: BabelNetQuery): List[BabelSense] =
    babelnet.getSensesFrom(query).asScala.toList
}
