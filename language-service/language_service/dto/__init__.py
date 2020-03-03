from collections import namedtuple

Definition = namedtuple("Definition", ["subdefinitions", "tag", "examples"])
Word = namedtuple("Word", ["token", "tag", "lemma"])
