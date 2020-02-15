import os

from flask import Flask, request
from flask_restful import Resource, Api

from ..nlp import tag

app = Flask(__name__)
api = Api(app)

API_BASE = os.getenv('APPLICATION_ROOT', "/")


class DocumentHandler(Resource):
    def post(self, language=None):
        request_json = request.get_json()

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        if "text" not in request_json:
            return {"error": "Text is required"}, 400

        text = request_json['text']

        try:
            words = tag(language, text)
            return {"words": words}, 200
        except Exception as e:
            print("Error getting words in %s for text: %s" % (language, text))
            print(e)
            return {"error": "An error occurred"}, 500


class HealthHandler(Resource):
    def get(self):
        return "Language service is up", 200


api.add_resource(DocumentHandler, API_BASE + '/tagging/v1/<string:language>/document')
api.add_resource(HealthHandler, '/')

if __name__ == '__main__':
    app.run(debug=True)
