"""
CEDICT is an open source Chinese dictionary.
This class provides definitions
"""
import json
from language_service.dto import ChineseDefinition

definitions = {}
with open("content/cedict.json") as cedict:
    for entry in json.load(cedict):
        simplified = entry["simplified"]
        definitions[simplified] = entry


def get_definitions(word):
    if word in definitions:
        definition = definitions[word]
        return ChineseDefinition(
            subdefinitions=definition["definitions"],
            pinyin=definition["pinyin"],
            simplified=definition["simplified"],
            traditional=definition["traditional"],
        )
    else:
        return None
