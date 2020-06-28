package com.foreignlanguagereader.api.domain.definition.entry.webster

import com.foreignlanguagereader.api.domain.definition.entry.webster.WebsterSource.WebsterSource
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

/**
  * This helper class exists to get rid of all the boilerplate with nested json parsers
  * The macros don't handle nested types without being told that they can.
  * So this tells them that they can
  * @param reads An implementation of json reading. Just needs to be in scope.
  * @param writes An implementation of json writing. Just needs to be in scope.
  * @tparam T The type we are helping with.
  */
class JsonHelper[T](implicit val reads: Reads[T],
                    implicit val writes: Writes[T]) {
  implicit val readsSeq: Reads[Seq[T]] = Reads.seq(reads)
  implicit val readsSeqSeq: Reads[Seq[Seq[T]]] = Reads.seq(readsSeq)
  implicit val readsSeqSeqSeq: Reads[Seq[Seq[Seq[T]]]] = Reads.seq(readsSeqSeq)
  implicit val writesSeq: Writes[Seq[T]] = Writes.seq(writes)
  implicit val writesSeqSeq: Writes[Seq[Seq[T]]] = Writes.seq(writesSeq)
  implicit val writesSeqSeqSeq: Writes[Seq[Seq[Seq[T]]]] =
    Writes.seq(writesSeqSeq)
}

/**
  * This file is for all the common parts of the webster schema
  * Anything that would be used by multiple dictionaries should be here
  */
object WebsterSource extends Enumeration {
  type WebsterSource = Value

  val LEARNERS: Value = Value("learners")
  val SPANISH: Value = Value("spanish")

  implicit val reads: Reads[WebsterSource] = Reads.enumNameReads(WebsterSource)
  implicit val writes: Writes[WebsterSource] = Writes.enumNameWrites
}

case class WebsterMeta(id: String,
                       uuid: String,
                       sort: String,
                       source: WebsterSource,
                       section: String,
                       stems: Seq[String],
                       offensive: Boolean)
object WebsterMeta {
  implicit val writes: Writes[WebsterMeta] = Json.writes[WebsterMeta]
  implicit val reads: Reads[WebsterMeta] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "uuid").read[String] and
      (JsPath \ "sort").read[String] and
      (JsPath \ "src").read[WebsterSource] and
      (JsPath \ "section").read[String] and
      (JsPath \ "stems").read[Seq[String]](Reads.seq[String]) and
      (JsPath \ "offensive").read[Boolean]
  )(WebsterMeta.apply _)
}

case class HeadwordInfo(headword: String,
                        pronunciations: Option[Seq[WebsterPronunciation]])
object HeadwordInfo {
  implicit val writes: Writes[HeadwordInfo] = Json.writes[HeadwordInfo]
  implicit val reads: Reads[HeadwordInfo] = ((JsPath \ "hw")
    .read[String] and (JsPath \ "prs").readNullable[Seq[WebsterPronunciation]])(
    HeadwordInfo.apply _
  )
}

case class WebsterPronunciation(writtenPronunciation: Option[String],
                                beforePronunciationLabel: Option[String],
                                afterPronunciationLabel: Option[String],
                                seperatorPunctuation: Option[String],
                                sound: Option[WebsterPronunciationSound])
object WebsterPronunciation {
  implicit val reads: Reads[WebsterPronunciation] = (
    (JsPath \ "mw").readNullable[String] and
      (JsPath \ "l").readNullable[String] and
      (JsPath \ "l2").readNullable[String] and
      (JsPath \ "pun").readNullable[String] and
      (JsPath \ "sound").readNullable[WebsterPronunciationSound]
  )(WebsterPronunciation.apply _)
  implicit val readsSeq: Reads[Seq[WebsterPronunciation]] = Reads.seq(reads)
  implicit val writes: Writes[WebsterPronunciation] =
    Json.writes[WebsterPronunciation]
  implicit val writesSeq: Writes[Seq[WebsterPronunciation]] = Writes.seq(writes)
}

case class WebsterPronunciationSound(audio: String,
                                     language: String = "en",
                                     country: String = "us") {
  val subdirectory: String = audio match {
    case bix if bix.startsWith("bix")                      => "bix"
    case gg if gg.startsWith("gg")                         => "gg"
    case number if number.matches("^[0-9\\.?!_\\-,;:]+.*") => "number"
    case _                                                 => audio.charAt(0).toString
  }

  // TODO maybe look at automatically setting this
  // For spanish change this to "es"
//  val language = "en"
  // For spanish definitions change this to "me" Depends on "lang": "es" in the metadata
//  val country = "us"

  val audioUrl: String =
    s"https://media.merriam-webster.com/audio/prons/$language/$country/mp3/$subdirectory/$audio.mp3"
}
object WebsterPronunciationSound {
  def createWithDefaults(audio: String, ref: String) =
    WebsterPronunciationSound(audio)
  implicit val reads: Reads[WebsterPronunciationSound] = (
    (JsPath \ "audio").read[String] and
      (JsPath \ "ref").read[String]
  )(WebsterPronunciationSound.createWithDefaults _)
  implicit val writes: Writes[WebsterPronunciationSound] =
    Json.writes[WebsterPronunciationSound]

}

case class WebsterInflection(inflection: Option[String],
                             inflectionCutback: Option[String],
                             inflectionLabel: Option[String],
                             pronunciation: Option[WebsterPronunciation])
object WebsterInflection {
  implicit val writes: Writes[WebsterInflection] =
    Json.writes[WebsterInflection]
  implicit val reads: Reads[WebsterInflection] = (
    (JsPath \ "if").readNullable[String] and
      (JsPath \ "ifc").readNullable[String] and
      (JsPath \ "il").readNullable[String] and
      (JsPath \ "prs").readNullable[WebsterPronunciation]
  )(WebsterInflection.apply _)
  implicit val readsSeq: Reads[Seq[WebsterInflection]] =
    Reads.seq[WebsterInflection]
}

case class WebsterDefinition(senseSequence: Option[Seq[WebsterSense]],
                             verbDivider: Option[String]) {
  // TODO verbDivider seems to be "transitive verb" or "intransitive verb". Opportunity for enum?
}
object WebsterDefinition {
  implicit val jsValueArray: Reads[Seq[JsValue]] = Reads.seq[JsValue]
  implicit val jsValueArrayArray: Reads[Seq[Seq[JsValue]]] =
    Reads.seq(jsValueArray)
  implicit val jsValueArrayArrayArray: Reads[Seq[Seq[Seq[JsValue]]]] =
    Reads.seq(jsValueArrayArray)

  // This really ugly hack is because webster uses triple nested lists
  // With both objects and strings in them.
  // This gives us a sane interface to work with
  def convertRawDefinitionToDefinitions(
    sseq: Option[Seq[Seq[Seq[JsValue]]]],
    verbDivider: Option[String]
  ): WebsterDefinition = {
    val senseSequence: Option[Seq[WebsterSense]] = if (sseq.isDefined) {
      val data: Seq[WebsterSense] =
        sseq
          .getOrElse(List())
          .flatten
          .map(row => row(1))
          .map {
            case o: JsObject => o.validate[WebsterSense]
          }
          .map {
            case JsSuccess(s, _) => s
            case JsError(e)      => throw new IllegalArgumentException(e.toString())
          }
      Some(data)
    } else {
      None
    }
    WebsterDefinition(senseSequence, verbDivider)
  }

  implicit val reads: Reads[WebsterDefinition] = ((JsPath \ "sseq")
    .readNullable[Seq[Seq[Seq[JsValue]]]] and (JsPath \ "vd")
    .readNullable[String])(convertRawDefinitionToDefinitions _)
}

case class WebsterSense(dt: Seq[Seq[String]],
                        et: Option[Seq[Seq[String]]],
                        ins: Option[Seq[WebsterInflection]],
                        prs: Option[Seq[WebsterPronunciation]],
                        sdsense: Option[WebsterSense],
                        sgram: Option[String],
                        sls: Option[Seq[String]],
                        // Sense number - basically an id - probably don't need this.
                        sn: Option[String],
                        variations: Option[Seq[WebsterVariant]],
                        senseDivider: Option[String]) {}
}
