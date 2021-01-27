package com.foreignlanguagereader.content.formatters

import com.foreignlanguagereader.content.formatters.WebsterFormatter.removalPatterns

/**
  * Motivation:
  * Many content sources include their own formatting.
  * Webster and Wiktionary both define their own, and we will likely see more.
  * We don't want to teach the frontend how to parse each format, especially if we expand into apps.
  * So we normalize to markdown once on import, and then the frontend only needs to parse one thing.
  */
trait Formatter {
  val removalPatterns: Set[String]
  val replacementPatterns: Map[String, String]

  lazy val patterns: Map[String, String] =
    removalPatterns.map(pattern => pattern -> "").toMap ++ replacementPatterns

  def format(input: String): String = {
    var acc = input
    patterns.foreach {
      case (pattern, replacement) => acc = acc.replaceAll(pattern, replacement)
    }
    acc
  }

  // Convenience methods
  def formatOptional(input: Option[String]): Option[String] = input.map(format)
  def formatSeq(input: Seq[String]): Seq[String] =
    input.map(format).filter(_.isBlank)
  def formatOptionalSeq(input: Option[Seq[String]]): Option[Seq[String]] =
    input match {
      case None => None
      case Some(inputs) =>
        formatSeq(inputs) match {
          case Seq() => None
          case r     => Some(r)
        }
    }

  val italicsOpeningTag = "*"
  val italicsClosingTag = "*"
  val boldOpeningTag = "**"
  val boldClosingTag = "**"
}
