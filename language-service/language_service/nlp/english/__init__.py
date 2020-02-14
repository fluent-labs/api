import en_core_web_sm
from ..common import is_not_punctuation, Word

parser = en_core_web_sm.load()


def tag_english(text):
    return {word.text: Word(token=word.text, tag=word.pos_, lemma=word.lemma_)
            for word in parser(text) if is_not_punctuation(word.text)}.values()
