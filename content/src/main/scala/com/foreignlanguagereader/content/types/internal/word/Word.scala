package com.foreignlanguagereader.content.types.internal.word

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.internal.word.Count.Count
import com.foreignlanguagereader.content.types.internal.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word.{
  numberFormat,
  punctuation
}
import com.foreignlanguagereader.content.types.internal.word.WordTense.WordTense
import com.foreignlanguagereader.dto.v1.word.WordDTO

case class Word(
    language: Language,
    token: String,
    tag: PartOfSpeech,
    lemma: String,
    gender: Option[GrammaticalGender],
    number: Option[Count],
    proper: Option[Boolean],
    tense: Option[WordTense],
    processedToken: String
) {
  val isPunctuation: Boolean = punctuation.contains(token)
  val isNumber: Boolean = token.matches(numberFormat)

  lazy val toDTO: WordDTO =
    new WordDTO(
      token,
      processedToken,
      tag.toString,
      lemma,
      isPunctuation,
      isNumber
    )
}
object Word {
  val punctuation = ",.?;'[]()（）`~!@#$%^&*/+_-=<>{}:，。？！·；：‘“、\"”《》"
  val numberFormat = "[0-9]+(.)*[0-9]*"

  def fromToken(token: String, language: Language): Word =
    Word(
      token = token,
      language = language,
      tag = PartOfSpeech.UNKNOWN,
      lemma = token,
      gender = None,
      number = None,
      proper = None,
      tense = None,
      processedToken = Word.processToken(token)
    )

  def processToken(token: String): String = token.toLowerCase
}
