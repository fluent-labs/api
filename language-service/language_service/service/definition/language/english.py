from language_service.service.definition.sources.wiktionary import Wiktionary

wiktionary = Wiktionary()


def get_english_definitions(word):
    return wiktionary.get_definitions("ENGLISH", word)
