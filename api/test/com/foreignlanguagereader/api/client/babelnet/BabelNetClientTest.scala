package com.foreignlanguagereader.api.client.babelnet

import akka.actor.ActorSystem
import com.foreignlanguagereader.api.client.common.CircuitBreakerAttempt
import com.foreignlanguagereader.domain.Language
import com.typesafe.config.ConfigFactory
import it.uniroma1.lcl.babelnet.BabelNetQuery
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatestplus.mockito.MockitoSugar

class BabelNetClientTest extends AsyncFunSpec with MockitoSugar {
  val holderMock: BabelnetClientHolder =
    mock[BabelnetClientHolder]

  val client = new BabelnetClient(
    holderMock,
    scala.concurrent.ExecutionContext.Implicits.global,
    ActorSystem("testActorSystem", ConfigFactory.load())
  )

  describe("A babelnet client") {
    describe("can handle the happy path") {
      when(holderMock.getSenses(any(classOf[BabelNetQuery])))
        .thenReturn(List())

      val result = client
        .getSenses(Language.SPANISH, "hola")
        .value
        .map {
          case CircuitBreakerAttempt(result) => assert(result.isEmpty)
          case _                             => fail("This isn't the happy path")
        }
    }
  }
}
