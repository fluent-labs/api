import pytest
from testfixtures import compare

from language_service.dto.definition import Definition
from language_service.service.definition import (
    get_definitions,
    get_definitions_for_group,
)

this_definition = Definition(subdefinitions=["This"])
is_definition = Definition(subdefinitions=["is"])
a_definition = Definition(subdefinitions=["a"])
test_definition = Definition(subdefinitions=["test"])


def get_definitions_successfully_mock(word):
    if word == "This":
        return this_definition
    elif word == "is":
        return is_definition
    elif word == "a":
        return a_definition
    elif word == "test":
        return test_definition


def test_get_definitions_for_group_happy_path(mocker):
    get_english_definitions = mocker.patch(
        "language_service.service.definition.get_english_definitions"
    )
    get_english_definitions.side_effect = get_definitions_successfully_mock

    result = get_definitions_for_group("ENGLISH", ["This", "is", "a", "test"])

    compare(
        result,
        [
            ("This", this_definition),
            ("is", is_definition),
            ("a", a_definition),
            ("test", test_definition),
        ],
    )


def get_definitions_with_failures_mock(word):
    if word == "This":
        return this_definition
    elif word == "is":
        return None
    elif word == "a":
        return a_definition
    elif word == "test":
        return None


def test_get_definitions_for_group_with_errors(mocker):
    get_english_definitions = mocker.patch(
        "language_service.service.definition.get_english_definitions"
    )
    get_english_definitions.side_effect = get_definitions_with_failures_mock

    result = get_definitions_for_group("ENGLISH", ["This", "is", "a", "test"])

    compare(
        result,
        [("This", this_definition), ("is", None), ("a", a_definition), ("test", None),],
    )


def test_can_pass_through_chinese_definitions(mocker):
    chinese = mocker.patch(
        "language_service.service.definition.get_chinese_definitions"
    )
    chinese.return_value = "所有的"

    assert get_definitions("CHINESE", "所有的") == "所有的"

    chinese.assert_called_once_with("所有的")


def test_can_pass_through_english_definitions(mocker):
    english = mocker.patch(
        "language_service.service.definition.get_english_definitions"
    )
    english.return_value = "any"

    assert get_definitions("ENGLISH", "any") == "any"

    english.assert_called_once_with("any")


def test_can_pass_through_spanish_definitions(mocker):
    spanish = mocker.patch(
        "language_service.service.definition.get_spanish_definitions"
    )
    spanish.return_value = "cualquier"

    assert get_definitions("SPANISH", "cualquier") == "cualquier"

    spanish.assert_called_once_with("cualquier")


def test_raises_exception_on_unknown_language(mocker):
    with pytest.raises(NotImplementedError):
        get_definitions("LAO", "ສິ່ງໃດກໍ່ຕາມ")
