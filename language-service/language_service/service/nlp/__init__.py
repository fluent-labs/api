from language_service.service.nlp.chinese import tag_chinese
from language_service.service.nlp.english import tag_english
from language_service.service.nlp.spanish import tag_spanish


def tag(language, text):
    if language == "CHINESE":
        return tag_chinese(text)
    elif language == "ENGLISH":
        return tag_english(text)
    elif language == "SPANISH":
        return tag_spanish(text)
    else:
        raise NotImplementedError("Unknown language requested: %s")
