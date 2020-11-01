package com.foreignlanguagereader.api.client.elasticsearch.searchstates

import com.foreignlanguagereader.api.client.elasticsearch.LookupAttempt
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import com.sksamuel.elastic4s.playjson._
import org.scalatest.funspec.AnyFunSpec

class ElasticsearchCacheRequestTest extends AnyFunSpec {
  describe("an elasticsearch cache request") {
    val indexRequests = (1 to 10)
      .map(
        number =>
          indexInto("attempts")
            .doc(
              LookupAttempt(
                index = "test",
                fields = Map(
                  s"Field ${1 * number}" -> s"Value ${1 * number}",
                  s"Field ${2 * number}" -> s"Value ${2 * number}"
                ),
                count = number
              )
          )
      )
      .toList
    val requests = ElasticsearchCacheRequest.fromRequests(indexRequests)

    it("can batch requests") {
      val batchOne = indexRequests.take(5)
      val batchTwo = indexRequests.drop(5)

      assert(requests(0) == ElasticsearchCacheRequest(batchOne))
      assert(requests(1) == ElasticsearchCacheRequest(batchTwo))
    }

    it("can handle retries") {
      requests.flatMap(_.retries).flatten.zip(indexRequests).foreach {
        case (elastic, index) =>
          assert(
            elastic == ElasticsearchCacheRequest(List(index), retried = true)
          )
      }
    }
  }
}
