import os
import logging

from flask import Flask
from flask_restful import Api

from language_service.cache import cache, DAY
from language_service.controller.definition import DefinitionController
from language_service.controller.document import DocumentController
from language_service.controller.health import HealthController

app = Flask(__name__)
api = Api(app)

config = {
    "DEBUG": False,
    "CACHE_TYPE": "redis",
    "CACHE_DEFAULT_TIMEOUT": DAY,
    "CACHE_KEY_PREFIX": "definitions",
    "CACHE_REDIS_HOST": os.getenv("CACHE_REDIS_HOST", "localhost"),
    "CACHE_REDIS_PORT": os.getenv("PORT", 6379),
}
if os.getenv("LOCAL", None) is None:
    config["CACHE_REDIS_PASSWORD"] = os.getenv("CACHE_REDIS_PASSWORD")
    config["CACHE_REDIS_DB"] = os.getenv("CACHE_REDIS_DB")

cache.init_app(
    app, config=config,
)

# Configure routes
api.add_resource(
    DefinitionController,
    "/v1/definitions/<string:language>/",
    "/v1/definition/<string:language>/<string:word>",
)
api.add_resource(DocumentController, "/v1/tagging/<string:language>/document")
api.add_resource(HealthController, "/health")

# Configure logging
# Necessary because gunicorn has its own logger
# And we want to log from the controller process not a worker
gunicorn_logger = logging.getLogger("gunicorn.error")
app.logger.handlers = gunicorn_logger.handlers
app.logger.setLevel(gunicorn_logger.level)

if __name__ == "__main__":
    app.run(debug=False)
