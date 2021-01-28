package com.foreignlanguagereader.content.formatters

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

  val italicsOpeningTag = "*"
  val italicsClosingTag = "*"
  val boldOpeningTag = "**"
  val boldClosingTag = "**"

  val punctuationMatches = "[,;]+"

  lazy val patterns: Map[String, String] =
    removalPatterns.map(pattern => pattern -> "").toMap ++ replacementPatterns

  def format(input: String): String = {
    val replaced = patterns.keySet.fold(input) {
      case (acc, pattern) =>
        acc.replaceAll(pattern, patterns.getOrElse(pattern, ""))
    }
    removeDuplicateSpaces(replaced).trim
  }

  def removeDuplicateSpaces(input: String): String =
    repeatUntilSettled(input, _.replaceAll("  ", " "))

  def repeatUntilSettled(
      before: String,
      operation: String => String
  ): String = {
    val after = operation.apply(before)
    if (before == after) after else repeatUntilSettled(after, operation)
  }

  // Convenience methods
  def formatOptional(input: Option[String]): Option[String] = input.map(format)
  def formatSeq(input: Seq[String]): Seq[String] =
    input.map(format).filter(!_.isBlank).filter(!_.matches(punctuationMatches))
  def formatList(input: List[String]): List[String] =
    input.map(format).filter(!_.isBlank)
  def formatOptionalSeq(input: Option[Seq[String]]): Option[Seq[String]] =
    input match {
      case None => None
      case Some(inputs) =>
        formatSeq(inputs) match {
          case Seq() => None
          case r     => Some(r)
        }
    }
  def formatOptionalList(input: Option[List[String]]): Option[List[String]] =
    input match {
      case None => None
      case Some(inputs) =>
        formatList(inputs) match {
          case List() => None
          case r      => Some(r)
        }
    }
}
