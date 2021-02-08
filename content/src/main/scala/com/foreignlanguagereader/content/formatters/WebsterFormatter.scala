package com.foreignlanguagereader.content.formatters

import play.api.Logger

import scala.collection.immutable.ListMap

object WebsterFormatter extends Formatter {
  val logger: Logger = Logger(this.getClass)

  // TODO
  // {d_link|dugong|dugong}
  // {wi}
  // {phrase}

  override val removalPatterns: Set[String] =
    Set(
      "\\[=[^]]*\\]", // Not sure what this is, undocumented but usually connected to phrases
      "\\{bc\\}",
      "\\{sc\\}",
      "\\{\\/sc\\}",
      "\\{sup\\}",
      "\\{\\/sup\\}",
      "\\{inf\\}",
      "\\{\\/inf\\}"
    )

  val captureGroupAllUntilBracket = "([^\\\\{]*)"

  val boldOpeningTag = "\\{b\\}" // {b}
  val boldClosingTag = "\\{\\\\\\/b\\}" // {/b}

  val italicOpeningTag = "\\{it\\}" // {it}
  val italicClosingTagOne = "\\{\\/it\\}" // {/it}
  val italicClosingTagTwo = "\\{\\\\\\/it\\}" // {\/it}

  override val replacementPatterns: ListMap[String, String] =
    ListMap(
      s"$boldOpeningTag$captureGroupAllUntilBracket$boldClosingTag" -> s"${FormattingTags.bold}$$1${FormattingTags.bold}",
      s"$italicOpeningTag$captureGroupAllUntilBracket$italicClosingTagOne" -> s"${FormattingTags.italic}$$1${FormattingTags.italic}",
      s"$italicOpeningTag$captureGroupAllUntilBracket$italicClosingTagTwo" -> s"${FormattingTags.italic}$$1${FormattingTags.italic}",
      "\\{ldquo\\}" -> "\"",
      "\\{rdquo\\}" -> "\""
    )
}
