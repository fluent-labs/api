from language_service.service.definition.sources.wiktionary import Wiktionary

wiktionary = Wiktionary()


def get_spanish_definitions(word):
    return wiktionary.get_definitions("SPANISH", word)
