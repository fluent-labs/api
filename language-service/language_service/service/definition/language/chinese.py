from language_service.dto.definition import ChineseDefinition
from language_service.service.definition.sources.cedict import CEDICT
from language_service.service.definition.sources.wiktionary import Wiktionary

cedict = CEDICT()


def get_chinese_definitions(word):
    # WiktionaryParser is not thread safe
    wiktionary = Wiktionary()

    wiktionary_definitions = wiktionary.get_definitions("CHINESE", word)
    cedict_definition = cedict.get_definitions(word)

    if wiktionary_definitions is not None and cedict_definition is not None:
        print("CHINESE - Found wiktionary and cedict definitions for %s" % word)
        definitions = []
        print(cedict_definition)
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
