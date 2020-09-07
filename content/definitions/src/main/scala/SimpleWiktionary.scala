case class SimpleWiktionary(text: String,
                            examples: String,
                            noun: String,
                            expression: String,
                            homophones: String,
                            prefix: String,
                            synonyms: String,
                            symbol: String,
                            initialism: String,
                            abbreviation: String,
                            suffix: String,
                            contraction: String,
                            notes: String,
                            conjunction: String,
                            antonym: String,
                            phrases: String,
                            antonyms: String,
                            gallery: String,
                            adverb: String,
                            determiner: String,
                            usage: String,
                            synonym: String,
                            acronym: String,
                            interjection: String,
                            determinative: String,
                            preposition: String,
                            adjective: String,
                            etymology: String,
                            pronoun: String,
                            phrase: String,
                            verb: String,
                            numerals: String,
                            pronunciation: String)

object SimpleWiktionary {
  val partsOfSpeech: List[String] = List(
    "abbreviation",
    "acronym",
    "adjective",
    "adjective 1",
    "adverb",
    "auxiliary verb",
    "compound determinative",
    "conjunction",
    "contraction",
    "demonstrative determiner",
    "determinative",
    "determiner",
    "expression",
    "initialism", // basically acronym
    "interjection",
    "noun",
    "noun 1",
    "noun 2",
    "noun 3",
    "prefix",
    "preposition",
    "pronoun",
    "proper noun",
    "suffix",
    "symbol",
    "verb",
    "verb 1",
    "verb 2"
  )

  val metaSections = List("pronunciation", "usage", "usage notes")

  // Parts of speech set here: http://www.lrec-conf.org/proceedings/lrec2012/pdf/274_Paper.pdf
  def mapWiktionaryPartOfSpeechToDomainPartOfSpeech(
    partOfSpeech: String
  ): String = partOfSpeech match {
    case "abbreviation"             => "Noun"
    case "acronym"                  => "Noun"
    case "adjective"                => "Adjective"
    case "adjective 1"              => "Adjective"
    case "adverb"                   => "Adverb"
    case "auxiliary verb"           => "Verb"
    case "compound determinative"   => "Determiner"
    case "conjunction"              => "Conjunction"
    case "contraction"              => "Unknown"
    case "demonstrative determiner" => "Determiner"
    case "determinative"            => "Determiner"
    case "determiner"               => "Determiner"
    case "expression"               => "Other"
    case "initialism"               => "Noun"
    case "interjection"             => "Particle"
    case "noun"                     => "Noun"
    case "noun 1"                   => "Noun"
    case "noun 2"                   => "Noun"
    case "noun 3"                   => "Noun"
    case "prefix"                   => "Affix"
    case "preposition"              => "Adposition"
    case "pronoun"                  => "Pronoun"
    case "proper noun"              => "Noun"
    case "suffix"                   => "Affix"
    case "symbol"                   => "Other"
    case "verb"                     => "Verb"
    case "verb 1"                   => "Verb"
    case "verb 2"                   => "Verb"
  }
}
