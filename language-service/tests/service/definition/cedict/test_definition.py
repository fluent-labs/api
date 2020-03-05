from testfixtures import compare
from language_service.dto import ChineseDefinition
from language_service.service.definition.cedict import CEDICT


def test_can_load_dictionary_file(mocker):
    cedict = CEDICT("./language_service/content/cedict.json")

    definition = cedict.get_definitions("定义")
    compare(
        definition,
        ChineseDefinition(
            pinyin="ding4 yi4",
            simplified="定义",
            traditional="定義",
            subdefinitions=["definition", "to define"],
        ),
    )


def test_returns_none_if_not_found(mocker):
    cedict = CEDICT("./language_service/content/cedict.json")
    assert cedict.get_definitions("无法找到") == None
