package com.foreignlanguagereader.api.domain.definition.combined

import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined
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

  val hsk: HSKLevel = ChineseDefinition.getHSK(simplified)

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

  lazy val hsk: Option[HSKHolder] = {
    Using(
      this.getClass
        .getResourceAsStream("/resources/definition/chinese/hsk.json")
    ) { file =>
      Json.parse(file).validate[HSKHolder]
    } match {
      case Success(result) =>
        result match {
          case JsSuccess(hskWords, _) =>
            logger.info("Successfully loaded hsk levels")
            Some(hskWords)
          case JsError(errors) =>
            logger.info(s"Failed to parse HSK levels: $errors")
            None
        }
      case Failure(exception) =>
        logger.info(
          s"Failed to load hsk levels: ${exception.getMessage}",
          exception
        )
        None
    }
  }
  def getHSK(simplified: String): HSKLevel = hsk match {
    case Some(h) => h.getLevel(simplified)
    case None    => HSKLevel.NONE
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

case class HSKHolder(HSK1: Set[String],
                     HSK2: Set[String],
                     HSK3: Set[String],
                     HSK4: Set[String],
                     HSK5: Set[String],
                     HSK6: Set[String]) {
  def getLevel(simplified: String): combined.HSKLevel.Value = simplified match {
    case s if HSK1.contains(s) => HSKLevel.ONE
    case s if HSK2.contains(s) => HSKLevel.TWO
    case s if HSK3.contains(s) => HSKLevel.THREE
    case s if HSK4.contains(s) => HSKLevel.FOUR
    case s if HSK5.contains(s) => HSKLevel.FIVE
    case s if HSK6.contains(s) => HSKLevel.SIX
    case _                     => HSKLevel.NONE
  }
}
object HSKHolder {
  implicit val reads: Reads[HSKHolder] = Json.reads[HSKHolder]
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
