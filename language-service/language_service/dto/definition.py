from marshmallow import Schema, fields


class Definition:
    def __init__(
        self,
        language="ENGLISH",
        source=None,
        subdefinitions=None,
        tag="",
        examples=None,
    ):
        """
        language: The language this word is defined in
        source: Where the definition came from
        subdefinitions: The different definitions for this meaning of the word
        tag: The part of speech
        examples: This word definition used in a sentence
        """
        self.language = language
        self.source = source
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples

    def set_subdefinitions(self, subdefinitions):
        self.subdefinitions = subdefinitions

    def __repr__(self):
        return (
            "Definition(language: %s, source: %s, subdefinitions: %s, tag: %s, examples: %s)"
            % (
                self.language,
                self.source,
                self.subdefinitions,
                self.tag,
                self.examples,
            )
        )


class ChineseDefinition(Definition):
    def __init__(
        self,
        language="ENGLISH",
        source=None,
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
        self.language = language
        self.source = source
        self.subdefinitions = subdefinitions
        self.tag = tag
        self.examples = examples
        self.pinyin = pinyin
        self.simplified = simplified
        self.traditional = traditional
        self.hsk = hsk

    def __repr__(self):
        return (
            "ChineseDefinition(language: %s, source: %s, subdefinitions: %s, tag: %s, examples: %s, pinyin: %s, simplified: %s, traditional: %s, hsk: %s)"
            % (
                self.language,
                self.source,
                self.subdefinitions,
                self.tag,
                self.examples,
                self.pinyin,
                self.simplified,
                self.traditional,
                self.hsk,
            )
        )


class DefinitionSchema(Schema):
    language = fields.Str()
    source = fields.Str()
    subdefinitions = fields.List(fields.Str())
    tag = fields.Str()
    examples = fields.List(fields.Str())
    pinyin = fields.Str()
    simplified = fields.Str()
    traditional = fields.Str()
    hsk = fields.Integer()
