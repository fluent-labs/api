import es_core_news_sm
from language_service.service.nlp.common import is_not_punctuation
from language_service.dto import Word

parser = es_core_news_sm.load()


def tag_spanish(text):
    unique_words = {
        word.text: Word(token=word.text, tag=word.pos_, lemma=word.lemma_)
        for word in parser(text)
        if is_not_punctuation(word.text)
    }
    return list(unique_words.values())
