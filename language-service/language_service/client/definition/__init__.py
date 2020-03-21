"""
Clients that get word definitions
"""
import logging
import os
import traceback

from abc import ABC, abstractmethod
from elasticsearch import Elasticsearch

from language_service.dto.definition import DefinitionSchema


ELASTICSEARCH_URL = os.getenv("ELASTICSEARCH_URL", "localhost:9200")

definition_schema = DefinitionSchema(many=True)
logger = logging.getLogger("LanguageService.client")


class DefinitionClient(ABC):
    def __init__(self, source):
        self.source = source
        self.es = Elasticsearch([ELASTICSEARCH_URL])

    def get_definitions(self, language, word):
        definitions = self.fetch_definitions(word, language)
        self.save_definitions_to_elasticsearch(language, definitions)
        return definitions

    @abstractmethod
    def fetch_definitions(self, language, word):
        """
        Implement this to actually get the definition
        """
        pass

    def get_definitions_from_elasticsearch(self, language, word):
        pass

    def save_definitions_to_elasticsearch(self, language, definitions):
        try:
            for definition in definition_schema.dump(definitions):
                definition["language"] = language
                self.es.index(
                    index="definitions", body=definition, doc_type=self.source
                )
        except Exception:
            logger.error("Error saving definition to elasticsearch: %s" % definition)
            stacktrace = traceback.format_exc()
            logger.error(stacktrace)
