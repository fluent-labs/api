"""
These objects are what will be used to respond to clients.
"""


class Definition:
    def __init__(self, subdefinitions=None, tag="", examples=None):
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples

    def set_subdefinitions(self, subdefinitions):
        self.subdefinitions = subdefinitions

    def to_json(self):
        return {
            "subdefinitions": self.subdefinitions,
            "tag": self.tag,
            "examples": self.examples,
        }


class ChineseDefinition(Definition):
    def __init__(
        self,
        subdefinitions=None,
        tag="",
        examples=None,
        pinyin=None,
        simplified=None,
        traditional=None,
        hsk=None,
    ):
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples
        self.pinyin = pinyin
        self.simplified = simplified
        self.traditional = traditional
        self.hsk = hsk

    def to_json(self):
        return {
            "subdefinitions": self.subdefinitions,
            "tag": self.tag,
            "examples": self.examples,
            "pinyin": self.pinyin,
            "simplified": self.simplified,
            "traditional": self.traditional,
            "hsk": self.hsk,
        }


class Word:
    def __init__(self, token="", tag="", lemma=""):  # nosec this is not a password
        self.token = token
        self.tag = tag
        self.lemma = lemma

    def to_json(self):
        return {
            "token": self.token,
            "tag": self.tag,
            "lemma": self.lemma,
        }
