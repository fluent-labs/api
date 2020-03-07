from language_service.service.definition.sources.wiktionary import Wiktionary


def get_spanish_definitions(word):
    # WiktionaryParser is not thread safe
    wiktionary = Wiktionary()

    return wiktionary.get_definitions("SPANISH", word)
