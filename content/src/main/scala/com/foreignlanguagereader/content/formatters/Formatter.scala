package com.foreignlanguagereader.content.formatters

/**
  * Motivation:
  * Many content sources include their own formatting.
  * Webster and Wiktionary both define their own, and we will likely see more.
  * We don't want to teach the frontend how to parse each format, especially if we expand into apps.
  * So we normalize to markdown once on import, and then the frontend only needs to parse one thing.
  */
trait Formatter {
  def format(input: String): String
}
