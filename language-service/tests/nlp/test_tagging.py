from language_service.nlp import tag
from language_service.nlp.common import Word


def test_chinese():
    words = tag("CHINESE", "测试已通过，因为返回了这些单词。")
    assert words == [
        Word(token="测试", tag="动词", lemma="测试"),
        Word(token="已", tag="副词", lemma="已"),
        Word(token="通过", tag="动词", lemma="通过"),
        Word(token="因为", tag="连词", lemma="因为"),
        Word(token="返回", tag="动词", lemma="返回"),
        Word(token="了", tag="助词", lemma="了"),
        Word(token="这些", tag="代词", lemma="这些"),
        Word(token="单词", tag="名词", lemma="单词"),
    ]


def test_english():
    words = tag("ENGLISH", "The test has passed because these words were returned.")
    assert words == [
        Word(token="The", tag="DET", lemma="the"),
        Word(token="test", tag="NOUN", lemma="test"),
        Word(token="has", tag="AUX", lemma="have"),
        Word(token="passed", tag="VERB", lemma="pass"),
        Word(token="because", tag="SCONJ", lemma="because"),
        Word(token="these", tag="DET", lemma="these"),
        Word(token="words", tag="NOUN", lemma="word"),
        Word(token="were", tag="AUX", lemma="be"),
        Word(token="returned", tag="VERB", lemma="return"),
    ]


def test_spanish():
    words = tag(
        "SPANISH", "La prueba ha pasado porque estas palabras fueron devueltas."
    )
    assert words == [
        Word(token="La", tag="DET", lemma="La"),
        Word(token="prueba", tag="NOUN", lemma="probar"),
        Word(token="ha", tag="AUX", lemma="haber"),
        Word(token="pasado", tag="VERB", lemma="pasar"),
        Word(token="porque", tag="SCONJ", lemma="porque"),
        Word(token="estas", tag="DET", lemma="este"),
        Word(token="palabras", tag="NOUN", lemma="palabra"),
        Word(token="fueron", tag="AUX", lemma="ser"),
        Word(token="devueltas", tag="VERB", lemma="devolver"),
    ]
