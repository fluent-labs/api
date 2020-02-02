# -*- coding: utf-8 -*-
from wiktionaryparser import WiktionaryParser


parser = WiktionaryParser()


def get_vocabulary(word, language):
    return parser.fetch(word, language)


def handler(event, context):
    print(event)

    language = event.get('language')
    word = event.get('word')

    print("Getting vocabulary for language: %s and word: %s" % (language, word))

    return {
        "statusCode": 200,
        "headers": {"Content-Type": "application/json"},
        "body": get_vocabulary(word, language)
        }
