package com.foreignlanguagereader.content.formatters

import play.api.Logger

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

  override val replacementPatterns: Map[String, String] =
    Map(
      "\\{b\\}" -> FormattingTags.bold,
      "\\{\\\\\\/b\\}" -> FormattingTags.bold,
      "\\{it\\}" -> FormattingTags.italic,
      "\\{\\\\\\/it\\}" -> FormattingTags.italic,
      "\\{\\/it\\}" -> FormattingTags.italic,
      "\\{ldquo\\}" -> "\"",
      "\\{rdquo\\}" -> "\""
    )
}
