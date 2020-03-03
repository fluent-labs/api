class Definition(dict):
    def __init__(self, subdefinitions=[], tag="", examples=[]):
        # Trick to enable JSON serialization
        dict.__init__(self, subdefinitions=subdefinitions, tag=tag, examples=examples)
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples


class Word(dict):
    def __init__(self, token="", tag="", lemma=""):
        # Trick to enable JSON serialization
        dict.__init__(self, token=token, tag=tag, lemma=lemma)
        self.token = token
        self.tag = tag
        self.lemma = lemma
