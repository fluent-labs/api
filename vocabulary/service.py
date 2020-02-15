# -*- coding: utf-8 -*-
import json
from wiktionaryparser import WiktionaryParser


parser = WiktionaryParser()


def return_error(message):
    return {
        "statusCode": 400,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps({"error": message}),
    }


def handler(event, context):
    try:
        body = json.loads(event.get("body"))
    except Exception as e:
        print(e)
        return_error("Could not parse request body, is it valid?")

    if body is None or body == "":
        return_error("You must post properties to this endpoint.")
    elif "language" not in body:
        return_error("You must specify a language.")
    elif "word" not in body:
        return_error("You must specify a word.")

    language = body["language"]
    word = body["word"]
    print("Getting vocabulary for language: %s and word: %s" % (language, word))

    try:
        return {
            "statusCode": 200,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps(parser.fetch(word, language)),
        }
    except Exception as e:
        print(e)
        return {
            "statusCode": 500,
            "headers": {"Content-Type": "application/json"},
            "body": json.dumps({"error": "Error getting data from Wikimedia"}),
        }
