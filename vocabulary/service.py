# -*- coding: utf-8 -*-
import json
from wiktionaryparser import WiktionaryParser


parser = WiktionaryParser()


def get_vocabulary(word, language):
    return parser.fetch(word, language)


def handler(event, context):
    error = None
    try:
        body = json.loads(event.get("body"))
    except Exception as e:
        print(e)
        error = "Could not parse request body, is it valid?"

    if body is None or body == "":
        error = "You must post properties to this endpoint."
    elif "language" not in body:
        error = "You must specify a language."
    elif "word" not in body:
        error = "You must specify a word."

    if error is not None:
        return {
            "statusCode": 400,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps({"error": error})
            }

    language = body["language"]
    word = body["word"]
    print("Getting vocabulary for language: %s and word: %s" % (language, word))

    try:
        return {
            "statusCode": 200,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps(get_vocabulary(word, language))
            }
    except Exception as e:
        print(e)
        return {
            "statusCode": 500,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps({"error": "Error getting data from Wikimedia"})
            }
