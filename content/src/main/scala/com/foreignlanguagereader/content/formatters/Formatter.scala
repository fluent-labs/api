package com.foreignlanguagereader.content.formatters

import scala.collection.immutable.ListMap

/** Motivation:
  * Many content sources include their own formatting.
  * Webster and Wiktionary both define their own, and we will likely see more.
  * We don't want to teach the frontend how to parse each format, especially if we expand into apps.
  * So we normalize to markdown once on import, and then the frontend only needs to parse one thing.
  */
trait Formatter {
  val removalPatterns: Set[String]
  val replacementPatterns: ListMap[String, String]

  // Custom hooks for anything that doesn't fit the regex model.
  def preFormat(input: String): String = input
  def postFormat(input: String): String = input

  val punctuationMatches = "[,;]+"

  lazy val patternsForMarkdown: ListMap[String, String] =
    ListMap(
      removalPatterns.map(pattern => pattern -> "").toList: _*
    ) ++ replacementPatterns

  lazy val patternsForPlaintext: Set[String] =
    removalPatterns ++ replacementPatterns.keySet

  // Used for definition extraction
  def format(input: String): String = {
    // Some patterns rely on beginning of the line
    input
      .split("\n")
      .map(preFormat)
      .map(formatLine)
      .map(postFormat)
      .mkString("\n")
  }

  def formatLine(input: String): String = {
    val replaced = patternsForMarkdown.keySet.fold(input) {
      case (acc, pattern) =>
        acc.replaceAll(pattern, patternsForMarkdown.getOrElse(pattern, ""))
    }
    removeDuplicateSpaces(replaced).trim
  }

  // Used for word count
  def removeFormattingInLine(input: String): String =
    removeDuplicateSpaces(patternsForPlaintext.fold(input) {
      case (acc, pattern) => acc.replaceAll(pattern, "")
    }).trim

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
