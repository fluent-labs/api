import re

PUNCTUATION_CHARACTERS = ",.?;'[]()（）`~!@#$%^&*/+_-=<>{}:，。？！·；：‘“、\"”《》"
number_pattern = re.compile("[0-9]+(.)*[0-9]*")


def is_not_punctuation(word):
    return word not in PUNCTUATION_CHARACTERS and number_pattern.match(word) is None
