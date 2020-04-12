import pytest
from unittest.mock import MagicMock

from werkzeug.exceptions import Unauthorized

from language_service.controller.common import (
    check_authentication,
    check_language,
    get_required_field,
)


def test_check_authentication_fails_request_with_no_token(mocker):
    os = mocker.patch("language_service.controller.common.os")
    os.getenv.return_value = "test_token"

    request = mocker.patch("language_service.controller.common.request")
    request.headers = {"headers": ["Content-Type", "application/json"]}

    controller = MagicMock()
    authenticated_controller = check_authentication(controller)

    with pytest.raises(Unauthorized):
        authenticated_controller(request)

    controller.assert_not_called()


def test_check_authentication_allows_request_with_valid_token(mocker):
    os = mocker.patch("language_service.controller.common.os")
    os.getenv.return_value = "test_token"

    request = mocker.patch("language_service.controller.common.request")
    request.headers = {"headers": ["Authentication", "test_token"]}

    controller = MagicMock()
    authenticated_controller = check_authentication(controller)

    with pytest.raises(Unauthorized):
        authenticated_controller(request)

    controller.assert_not_called()


def test_check_language(mocker):
    # Make sure we always require the language
    assert (None, "Language is required") == check_language(None)
    assert (None, "Language is required") == check_language("")

    # Check an unsupported language
    assert (None, "Language Klingon is not supported") == check_language("Klingon")

    # Check supported languages
    assert ("CHINESE", None) == check_language("CHINESE")
    assert ("ENGLISH", None) == check_language("ENGLISH")
    assert ("SPANISH", None) == check_language("SPANISH")

    # Check lowercase languages
    assert ("CHINESE", None) == check_language("chinese")
    assert ("ENGLISH", None) == check_language("english")
    assert ("SPANISH", None) == check_language("spanish")


def test_get_required_field_no_json(mocker):
    request = mocker.patch("language_service.controller.common.request")
    request.get_json.return_value = None

    assert (None, "Field my_field is required") == get_required_field(
        request, "my_field"
    )


def test_get_required_field_missing_field(mocker):
    request = mocker.patch("language_service.controller.common.request")
    request.get_json.return_value = {}

    assert (None, "Field my_field is required") == get_required_field(
        request, "my_field"
    )


def test_get_required_field_field_is_present(mocker):
    request = mocker.patch("language_service.controller.common.request")
    request.get_json.return_value = {"my_field": True}

    assert (True, None) == get_required_field(request, "my_field")
