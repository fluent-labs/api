package io.fluentlabs.domain.service

import io.fluentlabs.content.types.internal.word.Word
import io.fluentlabs.domain.client.circuitbreaker.CircuitBreakerAttempt
import io.fluentlabs.domain.repository.WordRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VocabularyService @Inject() (
    val wordRepository: WordRepository,
    implicit val ec: ExecutionContext
) {
  def setup(): Unit = {
    wordRepository.setup()
  }

  def getAllWords: Future[Seq[Word]] = {
    wordRepository.getAllWords
      .map({
        case CircuitBreakerAttempt(result) => result
        case _                             => List()
      })
      .map(words =>
        words.map(word =>
          Word(
            word.language,
            word.token,
            word.tag,
            word.lemma,
            None,
            None,
            None,
            None,
            word.token
          )
        )
      )
  }

  setup()
}
