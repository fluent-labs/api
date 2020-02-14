from .chinese import tag_chinese


def tag(language, text):
    if language == "CHINESE":
        return tag_chinese(text)
