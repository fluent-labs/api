# -*- coding: utf-8 -*-
from wiktionaryparser import WiktionaryParser


parser = WiktionaryParser()


def get_vocabulary(word, language):
    return parser.fetch(word, language)


def handler(event, context):
    language = event.get('language')
    word = event.get('word')
    return get_vocabulary(word, language)
