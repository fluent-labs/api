import re
from collections import namedtuple

PUNCTUATION_CHARACTERS = ",.?;'[]()（）`~!@#$%^&*/+_-=<>{}:，。？！·；：‘“、\"”《》"
number_pattern = re.compile("[0-9]+(.)*[0-9]*")

Word = namedtuple("Word", ["token", "tag", "lemma"])


def is_not_punctuation(word):
    return word not in PUNCTUATION_CHARACTERS and number_pattern.match(word) is None
