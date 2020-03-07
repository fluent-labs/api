from marshmallow import Schema, fields


class Word:
    def __init__(self, token="", tag="", lemma=""):  # nosec this is not a password
        self.token = token
        self.tag = tag
        self.lemma = lemma

    def __repr__(self):
        return "Word(token: %s, tag: %s, lemma: %s)" % (
            self.token,
            self.tag,
            self.lemma,
        )


class WordSchema(Schema):
    token = fields.Str()
    tag = fields.Str()
    lemma = fields.Str()
