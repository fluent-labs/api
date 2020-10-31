package com.foreignlanguagereader.domain.content.chinese

import com.github.houbb.opencc4j.util.ZhConverterUtil
import play.api.Logger

import scala.collection.JavaConverters._
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

object SimplifiedTraditionalConverter {
  val logger: Logger = Logger(this.getClass)

  val simplifiedRegex: Regex = """\[(.+)\]""".r
  def toSimplified(traditional: String): Option[String] = {
    Try({
      ZhConverterUtil.toSimple(traditional) match {
        case simplifiedRegex(simplified) => Some(simplified)
        case _                           => None
      }
    }) match {
      case Success(simplified) => simplified
      case Failure(e) =>
        logger.error(s"Failed to convert $traditional to simplified", e)
        None
    }
  }

  /**
    * Character simplification collapsed many characters into one.
    * Given this, one simplified character can have one or more traditional counterparts.
    * eg: 干 => [幹, 乾]
    *
    * We're making an opinionated choice to look at all possible combinations because:
    * - In practice the number should be very small.
    *   Most characters will only have one option, and those that do normally have only two
    * - We keep track of failed lookups and don't search them again.
    *   This means that we quickly solve which mapping is right and bound the time which we do unnecessary work
    *
    *   Why use this instead of the library method?
    *
    *   TODO TEST ME I AM VERY IMPORTANT
    * @param simplified The simplified character
    * @return
    */
  def toTraditional(simplified: String): Option[List[String]] = {
    Try(
      simplified.toCharArray.map(character =>
        Option(ZhConverterUtil.toTraditional(character))
      )
    ) match {
      case Success(values) if values.forall(_.isDefined) =>
        // Unbox the options, bring things back to scala types
        val cleaned = values.flatten.map(_.asScala.toList).toList
        // get all combinations of the results
        combineTraditionalResults(cleaned)
      case Success(values) =>
        // This is actually a failure case, see above
        val errors = simplified.toCharArray
          .zip(values)
          .filter {
            case (_, None) => true
            case _         => false
          }
          .map {
            case (char, _) => char
          }
        logger.error(
          s"Failed to convert $simplified to traditional because these characters failed conversion: ${errors
            .mkString(",")}"
        )
        None
      case Failure(e) =>
        logger.error(s"Failed to convert $simplified to traditional", e)
        None
    }
  }

  private[this] def combineTraditionalResults(
      results: List[List[String]]
  ): Option[List[String]] = {
    def generator(i: List[List[String]]): List[List[String]] =
      i match {
        case Nil => List(Nil)
        case h :: t =>
          for (j <- generator(t); i <- h) yield i :: j
      }
    // If the input is too large there is a chance of blowing the stack
    // That should only happen on highly unusual input and is not worth dealing with
    Try(generator(results)) match {
      case Success(s) => Some(s.map(_.mkString))
      case Failure(e) =>
        logger.error(
          s"Failed to interpret results from converting simplified characters to traditional for results $results",
          e
        )
        None
    }
  }
}
