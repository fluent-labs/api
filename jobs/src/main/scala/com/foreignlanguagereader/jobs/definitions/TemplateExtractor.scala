package com.foreignlanguagereader.jobs.definitions

object TemplateExtractor {
  val leftBrace = "\\{"
  val rightBrace = "\\}"
  val pipe = "\\|"
  val notPipeCaptureGroup = "([^|\\{]+)"
  val notRightBraceCaptureGroup = "([^\\}]*)"

  val templateRegex: String =
    leftBrace + leftBrace + notPipeCaptureGroup + pipe + notRightBraceCaptureGroup + rightBrace + rightBrace
  val extractTemplatesFromString: String => Array[Array[String]] =
    (input: String) =>
      templateRegex.r
        .findAllIn(input)
        .matchData
        .map(m => Array(m.group(1), m.group(2)))
        .toArray
