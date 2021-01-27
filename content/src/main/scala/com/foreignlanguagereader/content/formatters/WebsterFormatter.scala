package com.foreignlanguagereader.content.formatters

import play.api.Logger

object WebsterFormatter extends Formatter {
  val logger: Logger = Logger(this.getClass)

  val removalPatterns: Set[String] =
    Set(
      "\\[=[^]]*\\]",
      "\\{bc\\}",
      "\\{sc\\}",
      "\\{\\/sc\\}",
      "\\{sup\\}",
      "\\{\\/sup\\}",
      "\\{inf\\}",
      "\\{\\/inf\\}"
    )

  val replacementPatterns: Map[String, String] =
    Map(
      "\\{b\\}" -> "**",
      "\\{\\/b\\}" -> "**",
      "\\{it\\}" -> "*",
      "\\{\\/it\\}" -> "*",
      "\\{ldquo\\}" -> "\"",
      "\\{rdquo\\}" -> "\""
    )

  val patterns: Map[String, String] =
    removalPatterns.map(pattern => pattern -> "").toMap ++ replacementPatterns

  override def format(input: String): String = {
    var acc = input
    patterns.foreach {
      case (pattern, replacement) => acc = acc.replaceAll(pattern, replacement)
    }
    acc
  }
}
