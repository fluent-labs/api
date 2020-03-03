import json

definitions = {}
with open("content/cedict.json") as cedict:
    for entry in json.load(cedict):
        simplified = entry["simplified"]
        definitions[simplified] = entry


def get_definitions(word):
    if word in definitions:
        return definitions[word]
    else:
        return None
