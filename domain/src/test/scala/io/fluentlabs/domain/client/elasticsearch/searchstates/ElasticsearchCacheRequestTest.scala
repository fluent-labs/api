package io.fluentlabs.domain.client.elasticsearch.searchstates

import cats.syntax.all._
import io.fluentlabs.domain.client.elasticsearch.LookupAttempt
import io.fluentlabs.domain.util.ElasticsearchTestUtil
import org.scalatest.funspec.AnyFunSpec

class ElasticsearchCacheRequestTest extends AnyFunSpec {
  describe("an elasticsearch cache request") {
    val indexRequests = (1 to 10)
      .map(number => {
        val attempt = LookupAttempt(
          index = "test",
          fields = Map(
            s"Field ${1 * number}" -> s"Value ${1 * number}",
            s"Field ${2 * number}" -> s"Value ${2 * number}"
          ),
          count = number
        )
        ElasticsearchTestUtil.lookupIndexRequestFrom(attempt).asLeft
      })
      .toList
    val requests = ElasticsearchCacheRequest.fromRequests(indexRequests)

    it("can batch requests") {
      val batchOne = indexRequests.take(5)
      val batchTwo = indexRequests.drop(5)

      assert(requests.head == ElasticsearchCacheRequest(batchOne))
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
