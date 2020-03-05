"""
Scrapes Wiktionary for vocabulary definitions.
At current moment, this only provides definitions in English for all languages.
Wiktionary itself has definitions in many base languages, but the parser does not support it.
"""
from wiktionaryparser import WiktionaryParser
from dto import Definition

parser = WiktionaryParser()


def get_definitions(language, word):
    try:
        response = parser.fetch(word, language)
    except Exception as e:
        print("%s - Error fetching definition for %s" % (language, word))
        print(e)
        return None

    definitions = []
    for entry in response:
        if "definitions" in entry:
            for definition in entry["definitions"]:
                definitions.append(parse_definition(definition))

    return definitions


def parse_definition(definition):
    subdefinitions = definition["text"] if "text" in definition else None
    tag = definition["partOfSpeech"] if "partOfSpeech" in definition else None
    examples = definition["examples"] if "examples" in definition else None

    return Definition(subdefinitions=subdefinitions, tag=tag, examples=examples)
