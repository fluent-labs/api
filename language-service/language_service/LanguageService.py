import os

from flask import Flask, request
from flask_restful import Resource, Api

from nlp import tag
from vocab import get_definition

app = Flask(__name__)
api = Api(app)

API_BASE = os.getenv("APPLICATION_ROOT", "/")


class DocumentHandler(Resource):
    def post(self, language=None):
        request_json = request.get_json()

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        if request_json is None or "text" not in request_json:
            return {"error": "Text is required"}, 400

        text = request_json["text"]

        try:
            words = [
                {"token": word.token, "tag": word.tag, "lemma": word.lemma}
                for word in tag(language.upper(), text)
            ]
            return words, 200
        except Exception as e:
            print("Error getting words in %s for text: %s" % (language, text))
            print(e)
            return {"error": "An error occurred"}, 500


class HealthHandler(Resource):
    def get(self):
        return "Language service is up", 200


class VocabHandler(Resource):
    def get(self, language=None, word=None):
        if language is None or language == "":
            return {"error": "Language is required"}, 400

        if word is None or word == "":
            return {"error": "Word is required"}, 400

        try:
            return get_definition(language, word), 200
        except Exception as e:
            print("Error getting definition in %s for: %s" % (language, word))
            print(e)
            return {"error": "An error occurred"}, 500


api.add_resource(DocumentHandler, API_BASE + "/v1/tagging/<string:language>/document")
api.add_resource(HealthHandler, "/")
api.add_resource(
    VocabHandler, API_BASE + "/v1/vocabulary/<string:language>/<string:word>"
)

if __name__ == "__main__":
    app.run(debug=False)
