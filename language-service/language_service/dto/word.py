from marshmallow import Schema, fields


class Word:
    def __init__(self, token="", tag="", lemma=""):  # nosec this is not a password
        self.token = token
        self.tag = tag
        self.lemma = lemma


class WordSchema(Schema):
    token = fields.Str()
    tag = fields.Str()
    lemma = fields.Str()
