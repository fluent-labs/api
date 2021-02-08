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

      "\\{b\\}" -> FormattingTags.bold,
      "\\{\\\\\\/b\\}" -> FormattingTags.bold,
      "\\{it\\}" -> FormattingTags.italic,
      "\\{\\\\\\/it\\}" -> FormattingTags.italic,
      "\\{\\/it\\}" -> FormattingTags.italic,
  override val replacementPatterns: ListMap[String, String] =
    ListMap(
      "\\{ldquo\\}" -> "\"",
      "\\{rdquo\\}" -> "\""
    )
}
