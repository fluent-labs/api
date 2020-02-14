import re

__author__ = 'Lucas Kjaero'

PUNCTUATION_CHARACTERS = ",.?;'[]()（）`~!@#$%^&*/+_-=<>{}:，。？！·；：‘“、\"”《》"
number_pattern = re.compile("[0-9]+(.)*[0-9]*")


def is_not_punctuation(word):
    return word not in PUNCTUATION_CHARACTERS and number_pattern.match(word) is None


def drop_punctuation_and_numbers(iterable_text):
    """A generator that returns tokens in a text if they are not punctuation or numbers. Input must be iterable"""
    for token in iterable_text:
        token_string = str(token)
        if is_not_punctuation(token_string):
            yield token_string
