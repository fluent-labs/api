# -*- coding: utf-8 -*-
import json
from wiktionaryparser import WiktionaryParser


parser = WiktionaryParser()


def get_vocabulary(word, language):
    return parser.fetch(word, language)


def handler(event, context):
    print(event)

    body = json.loads(event.get('body'))

    language = body['language']
    word = body['word']

    print("Getting vocabulary for language: %s and word: %s" % (language, word))

    return {
        "statusCode": 200,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(get_vocabulary(word, language))
        }
