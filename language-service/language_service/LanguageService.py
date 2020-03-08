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

cache.init_app(
    app,
    config={
        "DEBUG": False,
        "CACHE_TYPE": "redis",
        "CACHE_DEFAULT_TIMEOUT": DAY,
        "CACHE_KEY_PREFIX": "definitions",
        "CACHE_REDIS_HOST": os.get_env("CACHE_REDIS_HOST"),
        "CACHE_REDIS_PORT": os.get_env("PORT"),
        "CACHE_REDIS_PASSWORD": os.get_env("CACHE_REDIS_PASSWORD"),
        "CACHE_REDIS_DB": os.get_env("CACHE_REDIS_DB"),
    },
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
