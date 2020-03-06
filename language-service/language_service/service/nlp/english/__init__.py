import en_core_web_sm
from language_service.service.nlp.common import is_not_punctuation
from language_service.dto import Word

parser = en_core_web_sm.load()


def tag_english(text):
    unique_words = {
        word.text: Word(token=word.text, tag=word.pos_, lemma=word.lemma_)
        for word in parser(text)
        if is_not_punctuation(word.text)
    }
    return list(unique_words.values())
