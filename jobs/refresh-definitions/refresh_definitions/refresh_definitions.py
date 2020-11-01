import logging
import os
import requests
import time

from elasticsearch import Elasticsearch

headers = {"Authorization": os.getenv("AUTH_TOKEN")}
ELASTICSEARCH_URL = os.getenv("ELASTICSEARCH_URL")
ELASTICSEARCH_USERNAME = os.getenv("ELASTICSEARCH_USERNAME")
ELASTICSEARCH_PASSWORD = os.getenv("ELASTICSEARCH_PASSWORD")

log = logging.getLogger("refresh_definitions")


class DefinitionRefresher:
    def __init__(self, language, scraped_sources=["WIKTIONARY"]):
        self.language = language
        self.scraped_sources = scraped_sources
        self.failed = []

        self.es = Elasticsearch(
            [ELASTICSEARCH_URL],
            http_auth=(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD),
        )

    def refresh_definitions(self, words):
        for word in words:
            self.remove_old_definitions(word)
            self.get_new_definitions(word)
            time.sleep(1)

    def remove_old_definitions(self, word):
        for source in self.scraped_sources:
            log.info(
                "Deleting definitions from %s in %s for %s"
                % (source, self.language, word)
            )
            self.es.delete_by_query(
                index="definitions",
                body={
                    "query": {
                        "bool": {
                            "must": [
                                {"match": {"language": self.language}},
                                {"match": {"source": source}},
                                {"match": {"token": word}},
                            ]
                        }
                    }
                },
            )

    def get_new_definitions(self, word):
        url = "https://language.foreignlanguagereader.com/v1/definition/%s/%s" % (
            self.language,
            word,
        )
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            log.info("Refreshed %s in %s" % (word, self.language))
        else:
            log.error("Failed to refresh %s in %s" % (word, self.language))
            self.failed.append(word)


def main():
    log.info("Refreshing English")
    with open("wordlist/google-10000-english.txt", "r") as words:
        english = DefinitionRefresher("ENGLISH")
        english.refresh_definitions(words)
    log.info("Refreshed English")


if __name__ == "__main__":
    main()
