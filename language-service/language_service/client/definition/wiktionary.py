"""
Scrapes Wiktionary for vocabulary definitions.
At current moment, this only provides definitions in English for all languages.
Wiktionary itself has definitions in many base languages, but the parser does not support it.
"""
import logging
import traceback
from wiktionaryparser import WiktionaryParser
from language_service.client.definition import DefinitionClient
from language_service.dto.definition import Definition

logger = logging.getLogger("LanguageService.definition.sources.wiktionary")


class Wiktionary(DefinitionClient):
    def __init__(self):
        super().__init__("WIKTIONARY")

    def fetch(self, language, word):
        # A fairly ugly shim for testability
        parser = WiktionaryParser()
        return parser.fetch(word, language)

    def fetch_definitions(self, language, word):
        logger.info("Wiktionary - getting definitions in %s for %s" % (language, word))
        try:
            response = self.fetch(language, word)
            logger.debug("Got response from wiktionary: %s" % response)
        except Exception:
            logger.error("%s - Error fetching definition for %s" % (language, word))
            stacktrace = traceback.format_exc()
            logger.error(stacktrace)
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

                    # Ignore malformed responses
                    if (
                        subdefinitions is not None
                        or tag is not None
                        or examples is not None
                    ):
                        definitions.append(
                            Definition(
                                token=word,
                                source="WIKTIONARY",
                                language=language,
                                subdefinitions=subdefinitions,
                                tag=tag,
                                examples=examples,
                            )
                        )
            else:
                logger.error("Malformed response returned: %s" % entry)

        logger.debug("Returning definitions %s" % definitions)
        return definitions
