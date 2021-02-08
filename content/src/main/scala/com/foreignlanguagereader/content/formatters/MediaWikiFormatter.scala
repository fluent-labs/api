package com.foreignlanguagereader.content.formatters

object MediaWikiFormatter extends Formatter {
  override val removalPatterns: Set[String] = Set("#\n")

  // Order matters here, use the most specific patterns before more general ones
  override val replacementPatterns: Map[String, String] =
    Map(
      "'''''" -> FormattingTags.boldAndItalic,
      "'''" -> FormattingTags.bold,
      "''" -> FormattingTags.italic,
      "=".repeat(6) -> "#".repeat(6),
      "=".repeat(5) -> "#".repeat(5),
      "=".repeat(4) -> "#".repeat(4),
      "=".repeat(3) -> "#".repeat(3),
      "=".repeat(2) -> "#".repeat(2),
      "=".repeat(1) -> "#".repeat(1),
      "----" -> "---"
    )
}
