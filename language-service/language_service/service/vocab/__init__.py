from wiktionaryparser import WiktionaryParser

parser = WiktionaryParser()


def get_definition(language, word):
    return parser.fetch(word, language)
