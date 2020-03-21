"""
Clients that get word definitions
"""

from abc import ABC, abstractmethod


class DefinitionClient(ABC):
    def __init__(self, source):
        self.source = source

    def get_definitions(self, word, language):
        return self.fetch_definitions(word, language)

    @abstractmethod
    def fetch_definitions(self, word, language):
        """
        Implement this to actually get the language
        """
        pass
