from language_service.LanguageService import app


def test_health(mocker):
    client = app.test_client()
    response = client.get("/health")

    assert response.get_json() == {"message": "Language service is up"}
    assert response.status == "200 OK"


def test_tagging(mocker):
    client = app.test_client()
    response = client.post(
        "/api/v1/tagging/ENGLISH/document",
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
