from language_service.service.definition import get_definitions


def test_calls_wiktionary_with_correct_parameters(mocker):
    wiktionary = mocker.patch("language_service.service.definition.english.Wiktionary")

    get_definitions("ENGLISH", "anything")

    wiktionary_instance = wiktionary.return_value
    wiktionary_instance.get_definitions.assert_called_once_with("ENGLISH", "anything")
