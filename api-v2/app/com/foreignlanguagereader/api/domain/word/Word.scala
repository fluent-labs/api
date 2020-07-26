package com.foreignlanguagereader.api.domain.word
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.Definition
import com.foreignlanguagereader.api.domain.word.Count.Count
import com.foreignlanguagereader.api.domain.word.GrammaticalGender.GrammaticalGender
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.api.domain.word.WordTense.WordTense
import com.foreignlanguagereader.api.dto.v1.document.WordDTO

case class Word(language: Language,
                token: String,
                tag: PartOfSpeech,
                lemma: String,
                definitions: Option[Seq[Definition]],
                gender: Option[GrammaticalGender],
                number: Option[Count],
                proper: Option[Boolean],
                tense: Option[WordTense],
                processedToken: String) {
  lazy val toDTO: WordDTO = {
    val defs = definitions match {
      case Some(d) => Some(d.map(_.toDTO))
      case None    => None
    }
    WordDTO(token, tag.toString, lemma, defs)
  }
}
object Word {
  def fromToken(token: String, language: Language) = Word(
    token = token,
    language = language,
    tag = PartOfSpeech.UNKNOWN,
    lemma = token,
    definitions = None,
    gender = None,
    number = None,
    proper = None,
    tense = None,
    processedToken = token
  )
}
