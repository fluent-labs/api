from marshmallow import Schema, fields


class Definition:
    def __init__(
        self, token, source, language, subdefinitions=None, tag="", examples=None
    ):
        """
        subdefinitions: The different definitions for this meaning of the word
        tag: The part of speech
        examples: This word definition used in a sentence
        """
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples
        self.token = token
        self.source = source
        self.language = language

    def set_subdefinitions(self, subdefinitions):
        self.subdefinitions = subdefinitions

    def __repr__(self):
        return "Definition(subdefinitions: %s, tag: %s, examples: %s)" % (
            self.subdefinitions,
            self.tag,
            self.examples,
        )


class ChineseDefinition(Definition):
    def __init__(
        self,
        token,
        source,
        subdefinitions=None,
        tag="",
        examples=None,
        pinyin=None,
        simplified=None,
        traditional=None,
        hsk=None,
    ):
        """
        All of the fields in a regular definition, but with some language specific bits
        pinyin: How you would pronounce/type this word
        simplified: The word in simplified characters
        traditional: The word in traditional characters
        hsk: The HSK difficulty rating (if it is on the lists)
        """
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples
        self.pinyin = pinyin
        self.simplified = simplified
        self.traditional = traditional
        self.hsk = hsk
        self.token = token
        self.source = source
        self.language = "CHINESE"

    def __repr__(self):
        return (
            "ChineseDefinition(subdefinitions: %s, tag: %s, examples: %s, pinyin: %s, simplified: %s, traditional: %s, hsk: %s)"
            % (
                self.subdefinitions,
                self.tag,
                self.examples,
                self.pinyin,
                self.simplified,
                self.traditional,
                self.hsk,
            )
        )


def make_definition_object(definition):
    if "pinyin" in definition:
        return ChineseDefinition(**definition)
    else:
        return Definition(**definition)


class DefinitionSchema(Schema):
    token = fields.Str()
    source = fields.Str()
    language = fields.Str()
    subdefinitions = fields.List(fields.Str())
    tag = fields.Str()
    examples = fields.List(fields.Str())
    pinyin = fields.Str()
    simplified = fields.Str()
    traditional = fields.Str()
    hsk = fields.Integer()
