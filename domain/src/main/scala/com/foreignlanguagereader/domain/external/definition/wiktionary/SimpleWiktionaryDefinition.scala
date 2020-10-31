package com.foreignlanguagereader.domain.external.definition.wiktionary

case class SimpleWiktionaryDefinition(
    // Required fields
    token: String,
    definition: String,
    tag: String,
    ipa: String,
    subdefinitions: Array[String],
    examples: Array[String],
    // Constants
    definitionLanguage: String,
    wordLanguage: String,
    source: String,
    // Nice extras
    antonyms: Array[String],
    homonyms: Array[String],
    homophones: Array[String],
    notes: Array[String],
    otherSpellings: Array[String],
    pronunciation: Array[String],
    related: Array[String],
    synonyms: Array[String],
    usage: Array[String]
)
