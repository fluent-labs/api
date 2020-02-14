from .chinese import tag_chinese
from .english import tag_english
from .spanish import tag_spanish


def tag(language, text):
    if language == "CHINESE":
        return tag_chinese(text)
    elif language == "ENGLISH":
        return tag_english(text)
    elif language == "SPANISH":
        return tag_spanish(text)
    else:
        print("Unknown language requested: %s" % language)
        # TODO throw an error
