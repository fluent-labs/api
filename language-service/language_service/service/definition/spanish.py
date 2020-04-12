from language_service.client.definition.wiktionary import Wiktionary


def get_spanish_definitions(word):
    # WiktionaryParser is not thread safe
    wiktionary = Wiktionary()

    return wiktionary.get_definitions("SPANISH", word)
