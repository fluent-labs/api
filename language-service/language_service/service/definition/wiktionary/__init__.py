"""
Scrapes Wiktionary for vocabulary definitions.
At current moment, this only provides definitions in English for all languages.
Wiktionary itself has definitions in many base languages, but the parser does not support it.
"""
from wiktionaryparser import WiktionaryParser
from language_service.dto import Definition


class Wiktionary:
    def __init__(self):
        self.parser = WiktionaryParser()

    def fetch(self, word, language):
        return self.parser.fetch(word, language)

    def get_definitions(self, language, word):
        try:
            response = self.fetch(word, language)
        except Exception as e:
            print("%s - Error fetching definition for %s" % (language, word))
            print(e)
            return None

        definitions = []
        for entry in response:
            if "definitions" in entry:
                for definition in entry["definitions"]:
                    subdefinitions = (
                        definition["text"] if "text" in definition else None
                    )
                    tag = (
                        definition["partOfSpeech"]
                        if "partOfSpeech" in definition
                        else None
                    )
                    examples = (
                        definition["examples"] if "examples" in definition else None
                    )
                    definitions.append(
                        Definition(
                            subdefinitions=subdefinitions, tag=tag, examples=examples
                        )
                    )

        return definitions
