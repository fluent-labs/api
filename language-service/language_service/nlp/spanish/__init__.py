import es_core_news_sm
from ..common import is_not_punctuation, Word

parser = es_core_news_sm.load()


def tag_spanish(text):
    return {word.text: Word(token=word.text, tag=word.pos_, lemma=word.lemma_)
            for word in parser(text) if is_not_punctuation(word.text)}.values()
