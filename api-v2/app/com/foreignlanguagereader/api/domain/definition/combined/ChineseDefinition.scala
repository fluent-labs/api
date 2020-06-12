package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.HSKLevel.HSKLevel
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.dto.v1.definition.ChineseDefinitionDTO
import play.api.Logger
import play.api.libs.json._

import scala.util.{Failure, Success, Using}

case class ChineseDefinition(override val subdefinitions: List[String],
                             override val tag: String,
                             override val examples: List[String],
                             pinyin: String = "",
                             simplified: String = "",
                             traditional: String = "",
                             hsk: HSKLevel = HSKLevel.NONE,
                             // These fields are needed for elasticsearch lookup
                             // But do not need to be presented to the user.
                             override val source: DefinitionSource =
                               DefinitionSource.MULTIPLE,
                             override val token: String = "")
    extends Definition {
  val language: Language = Language.CHINESE

  private val pronunciation = pinyin
    .split(" ")
    .map(
      pinyin =>
        ChineseDefinition.getPronunciation(pinyin) match {
          case Some(p) => p
          case None    => ChinesePronunciation("", "", "", "")
      }
    )
    .reduce(_ + _)
  val (ipa: String, zhuyin: String, wadeGiles: String) =
    (pronunciation.ipa, pronunciation.zhuyin, pronunciation.wade_giles)
  lazy val toDTO: ChineseDefinitionDTO =
    ChineseDefinitionDTO(
      subdefinitions,
      tag,
      examples,
      pinyin,
      simplified,
      traditional,
      ipa,
      zhuyin,
      wadeGiles,
      hsk
    )
}

// In here we have rich domain types
object ChineseDefinition {
  val logger: Logger = Logger(this.getClass)

  // This tags the words with zhuyin, wade giles, and IPA based on the pinyin.
  // This works because pinyin is a perfect sound system
  lazy private val pronunciations: Option[Map[String, ChinesePronunciation]] =
    Using(
      this.getClass
        .getResourceAsStream("/resources/definition/chinese/pronunciation.json")
    ) { file =>
      Json.parse(file).validate[Seq[ChinesePronunciation]]
    } match {
      case Success(result) =>
        result match {
          case JsSuccess(pronunciations, _) =>
            logger.info("Successfully loaded chinese pronunciations")
            Some(result.get.map(pron => pron.pinyin -> pron).toMap)
          case JsError(errors) =>
            logger.info(s"Failed to parse chinese pronunciations: $errors")
            None
        }
      case Failure(exception) =>
        logger.info(
          s"Failed to load chinese pronunciations: ${exception.getMessage}",
          exception
        )
        None
    }
  def getPronunciation(pinyin: String): Option[ChinesePronunciation] =
    pronunciations match {
      case Some(p) =>
        p.get(pinyin.replaceAll("[12345]+", "")) // remove tone marks
      case None => None
    }
}

case class ChinesePronunciation(pinyin: String,
                                ipa: String,
                                zhuyin: String,
                                wade_giles: String) {
  def +(b: ChinesePronunciation): ChinesePronunciation =
    ChinesePronunciation(
      pinyin + " " + b.pinyin,
      ipa + " " + b.ipa,
      zhuyin + " " + b.zhuyin,
      wade_giles + " " + b.wade_giles
    )
}
object ChinesePronunciation {
  implicit val reads: Reads[ChinesePronunciation] =
    Json.reads[ChinesePronunciation]
  implicit val readsSeq: Reads[Seq[ChinesePronunciation]] =
    Reads.seq(reads)
}

object HSKLevel extends Enumeration {
  type HSKLevel = Value

  val ONE: Value = Value("1")
  val TWO: Value = Value("2")
  val THREE: Value = Value("3")
  val FOUR: Value = Value("4")
  val FIVE: Value = Value("5")
  val SIX: Value = Value("6")
  val NONE: Value = Value("")

  implicit val hskLevelFormat: Format[HSKLevel] = new Format[HSKLevel] {
    def reads(json: JsValue): JsResult[Value] =
      JsSuccess(HSKLevel.withName(json.as[String]))
    def writes(level: HSKLevel.HSKLevel) = JsString(level.toString)
  }
}
