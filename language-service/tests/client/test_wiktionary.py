from testfixtures import compare
from unittest.mock import Mock
from language_service.dto.definition import Definition
from language_service.client.definition.wiktionary import Wiktionary


def test_can_fetch_definitions(mocker):
    wiktionary = Wiktionary()
    mocker.patch.object(wiktionary, "fetch")

    wiktionary.get_definitions("CHINESE", "定义")
    wiktionary.fetch.assert_called_once_with("CHINESE", "定义")


def test_can_parse_definitions(mocker):
    subdefinitions = ["definition 1", "definition 2"]
    examples = ["example 1", "example 2"]
    part_of_speech = "VERB"
    response = (
        {
            "definitions": [
                {
                    "text": subdefinitions,
                    "examples": examples,
                    "partOfSpeech": part_of_speech,
                },
                {"partOfSpeech": "NOUN"},
                {},
            ]
        },
    )

    wiktionary = Wiktionary()
    mocker.patch.object(wiktionary, "fetch")
    wiktionary.fetch.return_value = response

    definitions = wiktionary.get_definitions("ENGLISH", "something")
    compare(
        definitions,
        [
            Definition(
                subdefinitions=subdefinitions, examples=examples, tag=part_of_speech
            ),
            Definition(subdefinitions=None, examples=None, tag="NOUN"),
        ],
    )


def test_returns_none_if_error_occurs(mocker):
    wiktionary = Wiktionary()
    wiktionary.fetch = Mock(side_effect=KeyError("Error"))

    assert wiktionary.get_definitions("CHINESE", "无法找到") is None
