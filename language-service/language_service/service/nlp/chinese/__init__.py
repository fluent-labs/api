from collections import defaultdict
import jieba
import jieba.posseg as parser
from language_service.service.nlp.common import is_not_punctuation
from language_service.dto import Word

jieba.enable_paddle()

part_of_speech_mapping = defaultdict(lambda: "不知道词性")

part_of_speech_mapping.update(
    {
        "n": "普通名词",
        "f": "方位名词",
        "s": "处所名词",
        "t": "时间",
        "nr": "人名",
        "ns": "地名",
        "nt": "机构名",
        "nw": "作品名",
        "nz": "其他专名",
        "v": "普通动词",
        "vd": "动副词",
        "vn": "名动词",
        "a": "形容词",
        "ad": "副形词",
        "an": "名形词",
        "d": "副词",
        "m": "数量词",
        "q": "量词",
        "r": "代词",
        "p": "介词",
        "c": "连词",
        "u": "助词",
        "xc": "其他虚词",
        "w": "标点符号",
        "PER": "人名",
        "LOC": "地名",
        "ORG": "机构名",
        "TIME": "时间",
    }
)


def tag_chinese(text):
    unique_words = {
        word: Word(token=word, tag=part_of_speech_mapping[tag], lemma=word)
        for word, tag in parser.cut(text)
        if is_not_punctuation(word)
    }
    return list(unique_words.values())
