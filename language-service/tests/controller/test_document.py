from testfixtures import compare

from language_service.controller.document import DocumentController
from language_service.dto.word import Word


def test_definition_controller_checks_language_is_valid(mocker):
    controller = DocumentController()

    assert controller.post() == ({"error": "Language is required"}, 400)
    assert controller.post(language="UNSUPPORTED") == (
        {"error": "Language UNSUPPORTED is not supported"},
        400,
    )


def test_definition_controller_checks_text(mocker):
    controller = DocumentController()

    request = mocker.patch("language_service.controller.document.request")
    request.get_json.return_value = {}
    assert controller.post(language="ENGLISH") == (
        {"error": "Field text is required"},
        400,
    )


def test_definition_controller_happy_path(mocker):
    controller = DocumentController()

    request = mocker.patch("language_service.controller.document.request")
    request.get_json.return_value = {"text": "This is a very long test"}

    tag = mocker.patch("language_service.controller.document.tag")
    tag.return_value = [
        Word(lemma="this", token="This", tag="DET"),
        Word(lemma="be", token="is", tag="AUX"),
        Word(lemma="a", token="a", tag="DET"),
        Word(lemma="very", token="very", tag="ADV"),
        Word(lemma="long", token="long", tag="ADJ"),
        Word(lemma="test", token="test", tag="NOUN"),
    ]

    result, status = controller.post(language="ENGLISH")

    assert status == 200
    compare(
        result,
        [
            {"lemma": "this", "token": "This", "tag": "DET"},
            {"lemma": "be", "token": "is", "tag": "AUX"},
            {"lemma": "a", "token": "a", "tag": "DET"},
            {"lemma": "very", "token": "very", "tag": "ADV"},
            {"lemma": "long", "token": "long", "tag": "ADJ"},
            {"lemma": "test", "token": "test", "tag": "NOUN"},
        ],
    )
