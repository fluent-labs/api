from language_service.LanguageService import app
from language_service.dto.definition import Definition


def test_health(mocker):
    client = app.test_client()
    response = client.get("/health")

    assert response.get_json() == {"message": "Language service is up"}
    assert response.status == "200 OK"


def test_tagging(mocker):
    client = app.test_client()
    response = client.post(
        "/v1/tagging/ENGLISH/document",
        json={"text": "This is a test"},
        headers={"Authorization": "local"},
    )

    assert response.get_json() == [
        {"lemma": "this", "tag": "DET", "token": "This"},
        {"lemma": "be", "tag": "AUX", "token": "is"},
        {"lemma": "a", "tag": "DET", "token": "a"},
        {"lemma": "test", "tag": "NOUN", "token": "test"},
    ]
    assert response.status == "200 OK"


this_definition = Definition(subdefinitions=["This"])
is_definition = Definition(subdefinitions=["is"])
a_definition = Definition(subdefinitions=["a"])
test_definition = Definition(subdefinitions=["test"])


def get_definitions_successfully_mock(language, word):
    if word == "This":
        return [this_definition]
    elif word == "is":
        return [is_definition]
    elif word == "a":
        return [a_definition]
    elif word == "test":
        return [test_definition]


def test_definitions_slingle(mocker):
    get_english_definitions = mocker.patch(
        "language_service.service.definition.english.Wiktionary"
    )
    wiktionary = get_english_definitions.return_value
    wiktionary.get_definitions.side_effect = get_definitions_successfully_mock

    client = app.test_client()
    response = client.get(
        "/v1/definition/ENGLISH/test", headers={"Authorization": "local"},
    )

    assert response.status == "200 OK"
    assert response.get_json() == [
        {"examples": None, "subdefinitions": ["test"], "tag": ""}
    ]


def test_definitions_multiple(mocker):
    get_english_definitions = mocker.patch(
        "language_service.service.definition.english.Wiktionary"
    )
    wiktionary = get_english_definitions.return_value
    wiktionary.get_definitions.side_effect = get_definitions_successfully_mock

    client = app.test_client()
    response = client.post(
        "/v1/definitions/ENGLISH/",
        json={"words": ["This", "is", "a", "test"]},
        headers={"Authorization": "local"},
    )

    assert response.status == "200 OK"
    assert response.get_json() == {
        "This": [{"examples": None, "subdefinitions": ["This"], "tag": ""}],
        "a": [{"examples": None, "subdefinitions": ["a"], "tag": ""}],
        "is": [{"examples": None, "subdefinitions": ["is"], "tag": ""}],
        "test": [{"examples": None, "subdefinitions": ["test"], "tag": ""}],
    }
