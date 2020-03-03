import os

from flask import Flask
from flask_restful import Api

from controller import DefinitionController, DefinitionMultipleController, DocumentController, HealthController

app = Flask(__name__)
api = Api(app)

API_BASE = os.getenv("APPLICATION_ROOT", "/")

api.add_resource(
    DefinitionController, API_BASE + "/v1/definition/<string:language>/<string:word>"
)
api.add_resource(
    DefinitionMultipleController, API_BASE + "/v1/definitions/<string:language>/"
)
api.add_resource(
    DocumentController, API_BASE + "/v1/tagging/<string:language>/document"
)
api.add_resource(HealthController, "/health")

if __name__ == "__main__":
    app.run(debug=False)
