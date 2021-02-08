package com.foreignlanguagereader.content.formatters

object MediaWikiFormatter extends Formatter {
  override val removalPatterns: Set[String] = Set("#\n")

  // Order matters here, use the most specific patterns before more general ones
  override val replacementPatterns: Map[String, String] =
    Map(
      "'''''" -> FormattingTags.boldAndItalic,
      "'''" -> FormattingTags.bold,
      "''" -> FormattingTags.italic,
      "=".repeat(6) -> FormattingTags.levelSixHeading,
      "=".repeat(5) -> FormattingTags.levelFiveHeading,
      "=".repeat(4) -> FormattingTags.levelFourHeading,
      "=".repeat(3) -> FormattingTags.levelThreeHeading,
      "=".repeat(2) -> FormattingTags.levelTwoHeading,
      "=".repeat(1) -> FormattingTags.levelOneHeading,
      "----" -> "---"
    )
}
