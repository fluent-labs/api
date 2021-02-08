package com.foreignlanguagereader.content.formatters

import scala.collection.immutable.ListMap

object MediaWikiFormatter extends Formatter {
  val squareOpenBracket = "\\["
  val squareCloseBracket = "\\]"
  val anythingButCloseBracket = s"[^$squareCloseBracket]+"

  def nEqualSigns(n: Int): String = s"={$n}"
  val optionalWhitespace = "[ ]*"
  val anythingButEqualSign = "([^=]+)"
  val beginningOfLine = "^"

  def headerPattern(level: Int): String =
    beginningOfLine + nEqualSigns(
      level
    ) + optionalWhitespace + anythingButEqualSign + optionalWhitespace + nEqualSigns(
      level
    )

  // Order matters here, use the most specific patterns before more general ones
  override val replacementPatterns: ListMap[String, String] =
    ListMap(
      "'''''" -> FormattingTags.boldAndItalic,
      "'''" -> FormattingTags.bold,
      "''" -> FormattingTags.italic,
      headerPattern(6) -> s"${FormattingTags.levelSixHeading} $$1",
      headerPattern(5) -> s"${FormattingTags.levelFiveHeading} $$1",
      headerPattern(4) -> s"${FormattingTags.levelFourHeading} $$1",
      headerPattern(3) -> s"${FormattingTags.levelThreeHeading} $$1",
      headerPattern(2) -> s"${FormattingTags.levelTwoHeading} $$1",
      headerPattern(1) -> s"${FormattingTags.levelOneHeading} $$1",
      "----" -> FormattingTags.horizontalRule,
      s"$squareOpenBracket${squareOpenBracket}Category$anythingButCloseBracket$squareCloseBracket$squareCloseBracket" -> "",
      s"$squareOpenBracket$squareOpenBracket($anythingButCloseBracket)$squareCloseBracket$squareCloseBracket" -> "$1", // Generic links, [[link]] => link
      "<u>" -> FormattingTags.underlineOpen,
      "</u>" -> FormattingTags.underlineClose,
      "<s>" -> FormattingTags.strikethrough,
      "</s>" -> FormattingTags.strikethrough,
      "<del>" -> FormattingTags.strikethrough,
      "</del>" -> FormattingTags.strikethrough
    )

  override val removalPatterns: Set[String] = Set()
}
