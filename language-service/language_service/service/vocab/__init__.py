from .cedict import get_definitions as get_cedict_definitions
from .wiktionary import get_definitions as get_wiktionary_definitions


def get_definitions(language, word):
    if language == "CHINESE":
        wiktionary_definition = get_wiktionary_definitions(language, word)

        # {"traditional": "%", "simplified": "%", "pinyin": "pa1", "definitions": ["percent (Tw)"], "id": 0}
        cedict_definition = get_cedict_definitions(word)
        if cedict_definition:
            # The CEDICT definitions are more focused than wiktionary so we should prefer them.
            wiktionary_definition.subdefinitions = cedict_definition["definitions"]

            # Use language-specific information
            optional_properties = {"traditional": cedict_definition["traditional"], "simplified": cedict_definition["simplified"], "pinyin": cedict_definition["pinyin"]}
            wiktionary_definition.optional_properties = optional_properties

        return wiktionary_definition
    elif language == "ENGLISH":
        return get_wiktionary_definitions(language, word)
    elif language == "SPANISH":
        return get_wiktionary_definitions(language, word)
    else:
        raise NotImplementedError("Unknown language requested: %s")
