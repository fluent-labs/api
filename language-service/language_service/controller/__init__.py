import os
from flask import request
from flask_restful import Resource

from service.nlp import tag
from service.vocab import get_definitions

AUTH_TOKEN = os.getenv("AUTH_TOKEN")
SUPPORTED_LANGUAGES = ["CHINESE", "ENGLISH", "SPANISH"]


def is_authorized(request):
    return (
        "Authorization" in request.headers
        and request.headers["Authorization"] == AUTH_TOKEN
    )


class DefinitionController(Resource):
    def get(self, language=None, word=None):
        if not is_authorized:
            return {"error": "No authorization provided"}, 401

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        if language.upper() not in SUPPORTED_LANGUAGES:
            return {"error": "Language %s is not supported" % language}, 400

        if word is None or word == "":
            return {"error": "Word is required"}, 400

        try:
            return get_definitions(language, word), 200
        except Exception as e:
            print("Error getting definition in %s for: %s" % (language, word))
            print(e)
            return {"error": "An error occurred"}, 500


class DefinitionMultipleController(Resource):
    def post(self, language=None):
        if not is_authorized:
            return {"error": "No authorization provided"}, 401

        request_json = request.get_json()

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        if language.upper() not in SUPPORTED_LANGUAGES:
            return {"error": "Language %s is not supported" % language}, 400

        if request_json is None or "words" not in request_json:
            return {"error": "Words is required"}, 400

        words = request_json["words"]

        try:
            definitions = {
                word: get_definitions(language, word)
                for word in words
            }
            return definitions, 200
        except Exception as e:
            print("Error getting definitions in %s for words: %s" % (language, text))
            print(e)
            return {"error": "An error occurred"}, 500


class DocumentController(Resource):
    def post(self, language=None):
        if not is_authorized:
            return {"error": "No authorization provided"}, 401

        request_json = request.get_json()

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        if language.upper() not in SUPPORTED_LANGUAGES:
            return {"error": "Language %s is not supported" % language}, 400

        if request_json is None or "text" not in request_json:
            return {"error": "Text is required"}, 400

        text = request_json["text"]

        try:
            return tag(language.upper(), text), 200
        except Exception as e:
            print("Error getting words in %s for text: %s" % (language, text))
            print(e)
            return {"error": "An error occurred"}, 500


class HealthController(Resource):
    def get(self):
        return {"message": "Language service is up"}, 200
