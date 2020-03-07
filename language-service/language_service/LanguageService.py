import os
import logging

from flask import Flask
from flask_restful import Api

from language_service.controller.definition import DefinitionController
from language_service.controller.document import DocumentController
from language_service.controller.health import HealthController

app = Flask(__name__)
api = Api(app)

# Configure routes

API_BASE = os.getenv("APPLICATION_ROOT", "/api/")

api.add_resource(
    DefinitionController,
    API_BASE + "/v1/definitions/<string:language>/",
    API_BASE + "/v1/definition/<string:language>/<string:word>",
)
api.add_resource(
    DocumentController, API_BASE + "/v1/tagging/<string:language>/document"
)
api.add_resource(HealthController, "/health")

# Configure logging

# And then we pass it to the webserver's logger
gunicorn_logger = logging.getLogger("gunicorn.error")
app.logger.handlers = gunicorn_logger.handlers
app.logger.setLevel(gunicorn_logger.level)

if __name__ == "__main__":
    app.run(debug=False)
