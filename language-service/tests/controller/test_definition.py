from testfixtures import compare

from language_service.controller.definition import DefinitionController
from language_service.dto.definition import Definition, ChineseDefinition

test_first_subdefinitions = ["test (plural tests)", "A challenge, trial."]
test_second_subdefinitions = [
    "test (third-person singular simple present tests, present participle testing, simple past and past participle tested)",
    "To challenge.",
]
noun_tag = "noun"
test_first_examples = ["This is a test", "This is a second test"]
test_second_examples = ["This is a third test", "This is a fourth test"]

test_definitions_input = [
    Definition(
        subdefinitions=test_first_subdefinitions,
        tag=noun_tag,
        examples=test_first_examples,
    ),
    Definition(
        subdefinitions=test_second_subdefinitions,
        tag=noun_tag,
        examples=test_second_examples,
    ),
]
test_definitions_output = [
    {
        "subdefinitions": test_first_subdefinitions,
        "tag": noun_tag,
        "examples": test_first_examples,
    },
    {
        "subdefinitions": test_second_subdefinitions,
        "tag": noun_tag,
        "examples": test_second_examples,
    },
]

experiment_subdefinitions = [
    "experiment (plural experiments)",
    "A test under controlled conditions made to either demonstrate a known truth, examine the validity of a hypothesis, or determine the efficacy of something previously untried.",
    "(obsolete) Experience, practical familiarity with something.",
]
experiment_examples = [
    "South Korean officials announced last month that an experiment to create artificial rain did not provide the desired results.\nAudio ",
    "Audio ",
    "Pilot [...] Vpon his card and compas firmes his eye, / The maisters of his long experiment, / And to them does the steddy helme apply [...].",
]

experiment_definitions_input = [
    Definition(
        subdefinitions=experiment_subdefinitions,
        tag=noun_tag,
        examples=experiment_examples,
    )
]
experiment_definitions_output = [
    {
        "subdefinitions": experiment_subdefinitions,
        "tag": noun_tag,
        "examples": experiment_examples,
    },
]

好_word = "好"
好_subdefinitions = ["to be fond of", "to have a tendency to", "to be prone to"]
好_examples = [
    "Bǎ dōngxī shōushí qiánjìng, wǒ hǎo dǎsǎo fángjiān. [Pinyin]",
    "Put stuff away so that I can clean the room.",
]
好_pinyin = "hao4"

好_input = [
    ChineseDefinition(
        subdefinitions=好_subdefinitions,
        traditional=好_word,
        simplified=好_word,
        examples=好_examples,
        pinyin=好_pinyin,
    )
]
好_output = [
    {
        "traditional": 好_word,
        "subdefinitions": 好_subdefinitions,
        "hsk": None,
        "simplified": 好_word,
        "examples": 好_examples,
        "tag": "",
        "pinyin": 好_pinyin,
    }
]


def test_definition_controller_checks_language_is_valid(mocker):
    controller = DefinitionController()

    assert controller.get() == ({"error": "Language is required"}, 400)
    assert controller.get(language="UNSUPPORTED") == (
        {"error": "Language UNSUPPORTED is not supported"},
        400,
    )
    assert controller.post() == ({"error": "Language is required"}, 400)
    assert controller.post(language="UNSUPPORTED") == (
        {"error": "Language UNSUPPORTED is not supported"},
        400,
    )


def test_definition_controller_checks_word(mocker):
    controller = DefinitionController()

    assert controller.get(language="ENGLISH") == ({"error": "Word is required"}, 400,)

    request = mocker.patch("language_service.controller.definition.request")
    request.get_json.return_value = {}
    assert controller.post(language="ENGLISH") == (
        {"error": "Field words is required"},
        400,
    )


def test_single_definition_controller(mocker):
    controller = DefinitionController()

    get_definition = mocker.patch(
        "language_service.controller.definition.get_definitions"
    )
    get_definition.return_value = test_definitions_input

    result, status = controller.get(language="ENGLISH", word="test")

    assert status == 200
    compare(result, test_definitions_output)


def test_single_definition_controller_correctly_handles_chinese(mocker):
    controller = DefinitionController()

    get_definition = mocker.patch(
        "language_service.controller.definition.get_definitions"
    )
    get_definition.return_value = 好_input

    result, status = controller.get(language="CHINESE", word="好")

    assert status == 200
    compare(result, 好_output)


def test_multiple_definition_controller(mocker):
    controller = DefinitionController()

    get_definitions_for_group = mocker.patch(
        "language_service.controller.definition.get_definitions_for_group"
    )
    get_definitions_for_group.return_value = [
        ("test", test_definitions_input),
        ("experiment", experiment_definitions_input),
    ]

    request = mocker.patch("language_service.controller.definition.request")
    request.get_json.return_value = {"words": ["test", "experiment"]}

    result, status = controller.post(language="ENGLISH")

    assert status == 200

    get_definitions_for_group.assert_called_once_with("ENGLISH", {"test", "experiment"})

    assert "test" in result
    assert "experiment" in result

    compare(result["test"], test_definitions_output)
    compare(result["experiment"], experiment_definitions_output)
