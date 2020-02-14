from .chinese import tag_chinese
from .english import tag_english


def tag(language, text):
    if language == "CHINESE":
        return tag_chinese(text)
    elif language == "ENGLISH":
        return tag_english(text)
    else:
        print("Unknown language requested: %s" % language)
        # TODO throw an error
