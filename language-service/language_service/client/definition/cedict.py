"""
CEDICT is an open source Chinese dictionary.
This class provides definitions
"""
import json
import logging
from language_service.client.definition import DefinitionClient
from language_service.dto.definition import ChineseDefinition

logger = logging.getLogger("LanguageService.definition.sources.cedict")


class CEDICT(DefinitionClient):
    def __init__(self, dictionary_path="content/cedict.json"):
        super().__init__("CEDICT")
        self.dictionary_path = dictionary_path
        self.definitions = None

    def load_dictionary(self):
        self.definitions = {}
        with open(self.dictionary_path) as cedict:
            for entry in json.load(cedict):
                simplified = entry["simplified"]
                self.definitions[simplified] = entry

    def fetch_definitions(self, language, word):
        # Lazy load definitions to make unit testing possible
        if self.definitions is None:
            logger.info("Loading CEDICT dictionary")
            self.load_dictionary()

        logger.info("Getting definitions in CHINESE for %s" % word)

        if word in self.definitions:
            definition = self.definitions[word]
            return [
                ChineseDefinition(
                    token=word,
                    source="CEDICT",
                    subdefinitions=definition["definitions"],
                    pinyin=definition["pinyin"],
                    simplified=definition["simplified"],
                    traditional=definition["traditional"],
                )
            ]
        else:
            return None
