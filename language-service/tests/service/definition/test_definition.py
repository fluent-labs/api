import pytest
from language_service.service.definition import get_definitions


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
