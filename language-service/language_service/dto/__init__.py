class Definition:
    def __init__(self, subdefinitions=[], tag="", examples=[], optional_fields=[]):
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples
        self.optional_fields = optional_fields

    def set_subdefinitions(self, subdefinitions):
        self.subdefinitions = subdefinitions

    def set_optional_fields(self, optional_fields):
        self.optional_fields = optional_fields

    def to_json(self):
        return {
            "subdefinitions": self.subdefinitions,
            "tag": self.tag,
            "examples": self.examples,
            "optional_fields": self.optional_fields
        }


class Word(dict):
    def __init__(self, token="", tag="", lemma=""):
        # Trick to enable JSON serialization
        dict.__init__(self, token=token, tag=tag, lemma=lemma)
        self.token = token
        self.tag = tag
        self.lemma = lemma
