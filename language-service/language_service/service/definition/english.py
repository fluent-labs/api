from language_service.client.definition.wiktionary import Wiktionary


def get_english_definitions(word):
    # WiktionaryParser is not thread safe
    wiktionary = Wiktionary()

    return wiktionary.get_definitions("ENGLISH", word)
