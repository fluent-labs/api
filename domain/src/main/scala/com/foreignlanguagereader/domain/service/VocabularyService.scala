package com.foreignlanguagereader.domain.service

import io.fluentlabs.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.circuitbreaker.CircuitBreakerAttempt
import com.foreignlanguagereader.domain.repository.WordRepository

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
