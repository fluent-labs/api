from language_service.controller.health import HealthController


def test_health_check_exists(mocker):
    controller = HealthController()

    assert controller.get() == ({"message": "Language service is up"}, 200)
