"""
Clients that get word definitions
"""
import logging
import os
import traceback

from abc import ABC, abstractmethod
from elasticsearch import Elasticsearch

from language_service.dto.definition import DefinitionSchema, make_definition_object


ELASTICSEARCH_URL = os.getenv("ELASTICSEARCH_URL", "localhost:9200")
ELASTICSEARCH_USERNAME = os.getenv("ELASTICSEARCH_USERNAME", None)
ELASTICSEARCH_PASSWORD = os.getenv("ELASTICSEARCH_PASSWORD", None)

definition_schema = DefinitionSchema(many=True)
logger = logging.getLogger("LanguageService.client")


class DefinitionClient(ABC):
    """
    Base class to cache definitions in elasticsearch.

    To use:
    Override fetch_definitions(language, word) with your source implementation
    And call super().__init__(name) with the name of your source
    """

    def __init__(self, source):
        self.source = source

        if ELASTICSEARCH_USERNAME is not None and ELASTICSEARCH_PASSWORD is not None:
            self.es = Elasticsearch(
                [ELASTICSEARCH_URL],
                http_auth=(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD),
            )
        else:
            self.es = Elasticsearch([ELASTICSEARCH_URL])

    def get_definitions(self, language, word):
        logger.info(
            "Getting definitions in %s for %s using %s" % (language, word, self.source)
        )
        elasticsearch_definitions = self.get_definitions_from_elasticsearch(
            language, word
        )

        if elasticsearch_definitions:
            logger.info(
                "Returning elasticsearch results for definitions in %s for %s using %s"
                % (language, word, self.source)
            )
            return elasticsearch_definitions

        else:
            logger.info(
                "No elasticsearch results for definitions in %s for %s using %s"
                % (language, word, self.source)
            )
            definitions = self.fetch_definitions(language, word)

            if definitions is not None:
                self.save_definitions_to_elasticsearch(language, word, definitions)

            return definitions

    @abstractmethod
    def fetch_definitions(self, language, word):
        """
        Implement this to actually get the definition
        """
        raise NotImplementedError("Definition client class didn't implement this")

    def get_definitions_from_elasticsearch(self, language, word):
        logger.info(
            "Checking elasticsearch for definitions in %s for %s using %s"
            % (language, word, self.source)
        )

        try:
            result = self.es.search(
                index="definitions",
                body={
                    "query": {
                        "bool": {
                            "must": [
                                {"match": {"language": language}},
                                {"match": {"source": self.source}},
                                {"match": {"token": word}},
                            ]
                        }
                    }
                },
            )
            hits = result["hits"]["hits"]

            if len(hits) > 0:
                definitions = []
                for hit in hits:
                    source = hit["_source"]

                    # Needed to keep from including elasticsearch implementation fields
                    definition = {
                        key: val
                        for key, val in source.items()
                        if key not in ["language", "source", "token"]
                    }
                    definitions.append(make_definition_object(definition))
                return definitions
            else:
                return None
        except Exception:
            logger.error(
                "Error getting definitions from elasticsearch in %s for %s"
                % (language, word)
            )
            stacktrace = traceback.format_exc()
            logger.error(stacktrace)

    def save_definitions_to_elasticsearch(self, language, word, definitions):
        logger.info(
            "Saving to elasticsearch definitions in %s for %s using %s"
            % (language, word, self.source)
        )
        try:
            for definition in definition_schema.dump(definitions):
                # Fields needed to find the definition again
                definition["language"] = language
                definition["source"] = self.source
                definition["token"] = word
                self.es.index(
                    index="definitions", body=definition, doc_type="definition"
                )
        except Exception:
            logger.error("Error saving definition to elasticsearch: %s" % definition)
            stacktrace = traceback.format_exc()
            logger.error(stacktrace)
