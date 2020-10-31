package com.foreignlanguagereader.domain.word
import com.foreignlanguagereader.domain.Language.Language
import com.foreignlanguagereader.domain.definition.Definition
import com.foreignlanguagereader.domain.word.Count.Count
import com.foreignlanguagereader.domain.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.domain.word.WordTense.WordTense
import com.foreignlanguagereader.dto.v1.document.WordDTO

case class Word(
    language: Language,
    token: String,
    tag: PartOfSpeech,
    lemma: String,
    definitions: Seq[Definition],
    gender: Option[GrammaticalGender],
    number: Option[Count],
    proper: Option[Boolean],
    tense: Option[WordTense],
    processedToken: String
) {
  lazy val toDTO: WordDTO = {
    val defs = definitions.map(_.toDTO)
    WordDTO(token, tag.toString, lemma, defs)
  }
}
object Word {
  def fromToken(token: String, language: Language): Word =
    Word(
      token = token,
      language = language,
      tag = PartOfSpeech.UNKNOWN,
      lemma = token,
      definitions = List(),
      gender = None,
      number = None,
      proper = None,
      tense = None,
      processedToken = token
    )
}
