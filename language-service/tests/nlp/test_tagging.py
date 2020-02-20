import pytest
from language_service.nlp import tag
from language_service.nlp.common import Word


def test_can_tag_chinese():
    words = tag("CHINESE", "测试已通过，因为返回了这些单词。")
    assert words == [
        Word(token="测试", tag="名动词", lemma="测试"),
        Word(token="已", tag="副词", lemma="已"),
        Word(token="通过", tag="介词", lemma="通过"),
        Word(token="因为", tag="连词", lemma="因为"),
        Word(token="返回", tag="普通动词", lemma="返回"),
        Word(token="了", tag="不知道词性", lemma="了"),
        Word(token="这些", tag="代词", lemma="这些"),
        Word(token="单词", tag="普通名词", lemma="单词"),
    ]


def test_chinese_tagging_no_duplicates():
    words = tag(
        "CHINESE",
        "石室诗士施氏，嗜狮，誓食十狮。氏时时适市视狮。十时，适十狮适市。 是时，适施氏适市。氏视是十狮，恃矢势，使是十狮逝世。氏拾是十狮尸，适石室。石室湿，氏使侍拭石室。石室拭，氏始试食是十狮尸。食时，始识是十狮，实十石狮尸。试释是事。",
    )
    assert words == [
        Word(token="石室", tag="普通名词", lemma="石室"),
        Word(token="诗士", tag="普通名词", lemma="诗士"),
        Word(token="施氏", tag="人名", lemma="施氏"),
        Word(token="嗜", tag="不知道词性", lemma="嗜"),
        Word(token="狮", tag="普通名词", lemma="狮"),
        Word(token="誓食", tag="普通名词", lemma="誓食"),
        Word(token="十", tag="数量词", lemma="十"),
        Word(token="氏", tag="不知道词性", lemma="氏"),
        Word(token="时时", tag="普通名词", lemma="时时"),
        Word(token="适市", tag="普通动词", lemma="适市"),
        Word(token="视狮", tag="普通名词", lemma="视狮"),
        Word(token="时", tag="不知道词性", lemma="时"),
        Word(token="适", tag="普通动词", lemma="适"),
        Word(token=" ", tag="不知道词性", lemma=" "),
        Word(token="是", tag="普通动词", lemma="是"),
        Word(token="适施", tag="普通动词", lemma="适施"),
        Word(token="视", tag="不知道词性", lemma="视"),
        Word(token="恃", tag="不知道词性", lemma="恃"),
        Word(token="矢", tag="不知道词性", lemma="矢"),
        Word(token="势", tag="不知道词性", lemma="势"),
        Word(token="使", tag="普通动词", lemma="使"),
        Word(token="逝世", tag="普通动词", lemma="逝世"),
        Word(token="拾", tag="普通动词", lemma="拾"),
        Word(token="狮尸", tag="普通名词", lemma="狮尸"),
        Word(token="湿", tag="形容词", lemma="湿"),
        Word(token="侍", tag="人名", lemma="侍"),
        Word(token="拭", tag="不知道词性", lemma="拭"),
        Word(token="始", tag="不知道词性", lemma="始"),
        Word(token="试食", tag="普通名词", lemma="试食"),
        Word(token="食时", tag="普通名词", lemma="食时"),
        Word(token="始识", tag="普通动词", lemma="始识"),
        Word(token="实", tag="形容词", lemma="实"),
        Word(token="石狮", tag="人名", lemma="石狮"),
        Word(token="尸", tag="普通名词", lemma="尸"),
        Word(token="试释", tag="普通动词", lemma="试释"),
        Word(token="事", tag="普通名词", lemma="事"),
    ]


def test_can_tag_english():
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


def test_english_tagging_no_duplicates():
    words = tag(
        "ENGLISH", "This is the song that never ends, it goes on and on my friends"
    )
    assert words == [
        Word(token="This", tag="DET", lemma="this"),
        Word(token="is", tag="AUX", lemma="be"),
        Word(token="the", tag="DET", lemma="the"),
        Word(token="song", tag="NOUN", lemma="song"),
        Word(token="that", tag="PRON", lemma="that"),
        Word(token="never", tag="ADV", lemma="never"),
        Word(token="ends", tag="VERB", lemma="end"),
        Word(token="it", tag="PRON", lemma="-PRON-"),
        Word(token="goes", tag="VERB", lemma="go"),
        Word(token="on", tag="ADP", lemma="on"),
        Word(token="and", tag="CCONJ", lemma="and"),
        Word(token="my", tag="PRON", lemma="-PRON-"),
        Word(token="friends", tag="NOUN", lemma="friend"),
    ]


def test_can_tag_spanish():
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


def test_spanish_tagging_no_duplicates():
    words = tag("SPANISH", "Hola, mi llamo Lucas. Hola Lucas")
    assert words == [
        Word(token="Hola", tag="PROPN", lemma="Hola"),
        Word(token="mi", tag="DET", lemma="mi"),
        Word(token="llamo", tag="NOUN", lemma="llamar"),
        Word(token="Lucas", tag="PROPN", lemma="Lucas"),
    ]


def test_raises_exception_on_unknown_language():
    with pytest.raises(NotImplementedError):
        tag("KLINGON", "We can't handle this sentence")
