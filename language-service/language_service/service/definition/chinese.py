import logging

from language_service.client.definition.cedict import CEDICT
from language_service.client.definition.wiktionary import Wiktionary

cedict = CEDICT()
logger = logging.getLogger("LanguageService.definition.language.chinese")


def get_chinese_definitions(word):
    # WiktionaryParser is not thread safe
    wiktionary = Wiktionary()

    wiktionary_definitions = wiktionary.get_definitions("CHINESE", word)
    cedict_definitions = cedict.get_definitions("CHINESE", word)

    result = []
    result.extend(wiktionary_definitions)
    result.extend(cedict_definitions)
    return result
