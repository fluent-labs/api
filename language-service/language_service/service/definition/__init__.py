"""
Where you put definition business logic.
We combine different data sources as available to deliver the best definitions.
"""
from language_service.dto import ChineseDefinition
from language_service.service.definition.cedict import CEDICT
from language_service.service.definition.wiktionary import (
    get_definitions as get_wiktionary_definitions,
)

cedict = CEDICT()


def get_definitions(language, word):
    if language == "CHINESE":
        return get_chinese_definitions(word)

    elif language == "ENGLISH":
        return get_wiktionary_definitions(language, word)

    elif language == "SPANISH":
        return get_wiktionary_definitions(language, word)

    else:
        raise NotImplementedError("Unknown language requested: %s")


def get_chinese_definitions(word):
    wiktionary_definitions = get_wiktionary_definitions("CHINESE", word)
    cedict_definition = cedict.get_definitions(word)

    if wiktionary_definitions is not None and cedict_definition is not None:
        print("CHINESE - Found wiktionary and cedict definitions for %s" % word)
        definitions = []
        for definition in wiktionary_definitions:
            # The CEDICT definitions are more focused than wiktionary so we should prefer them.
            subdefinitions = (
                cedict_definition.subdefinitions
                if cedict_definition.subdefinitions is not None
                else definition.subdefinitions
            )

            improved_definition = ChineseDefinition(
                subdefinitions=subdefinitions,
                tag=definition.tag,
                examples=definition.examples,
                pinyin=cedict_definition.pinyin,
                simplified=cedict_definition.simplified,
                traditional=cedict_definition.traditional,
            )
            definitions.append(improved_definition)
        return definitions

    elif wiktionary_definitions is not None and cedict_definition is None:
        print("CHINESE - Only found wiktionary definition for %s" % word)
        return wiktionary_definitions

    elif wiktionary_definitions is None and cedict_definition is not None:
        print("CHINESE - Only found cedict definition for %s" % word)
        return [cedict_definition]

    else:
        print("CHINESE - No definition found for %s" % word)
        return None
