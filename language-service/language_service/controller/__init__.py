"""
Handles the REST logic for communicating with clients.
"""

import os
import traceback
from multiprocessing.dummy import Pool

from flask import request
from flask_restful import Resource

from language_service.service.nlp import tag
from language_service.service.definition import get_definitions

AUTH_TOKEN = os.getenv("AUTH_TOKEN")
SUPPORTED_LANGUAGES = ["CHINESE", "ENGLISH", "SPANISH"]


def is_authorized(request):
    return (
        "Authorization" in request.headers
        and request.headers["Authorization"] == AUTH_TOKEN
    )


def fetch_definitions(language, word):
    """
    Helper method to handle getting and deserializing a single definition
    """
    definitions = get_definitions(language, word)

    if definitions is None:
        return None, word
    else:
        return (
            [
                definition.to_json()
                for definition in definitions
                if definition is not None
            ],
            word,
        )


class DefinitionController(Resource):
    def get(self, language=None, word=None):
        if not is_authorized:
            return {"error": "No authorization provided"}, 401

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        language = language.upper()
        if language not in SUPPORTED_LANGUAGES:
            return {"error": "Language %s is not supported" % language}, 400

        if word is None or word == "":
            return {"error": "Word is required"}, 400

        print("%s - Getting definition for %s" % (language, word))

        try:
            definitions, _word = fetch_definitions(language, word)
            return definitions, 200
        except Exception:
            print("Error getting definition in %s for: %s" % (language, word))
            stacktrace = traceback.format_exc()
            print(stacktrace)
            return {"error": "An error occurred"}, 500


class DefinitionMultipleController(Resource):
    def post(self, language=None):
        if not is_authorized:
            return {"error": "No authorization provided"}, 401

        request_json = request.get_json()

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        language = language.upper()
        if language not in SUPPORTED_LANGUAGES:
            return {"error": "Language %s is not supported" % language}, 400

        if request_json is None or "words" not in request_json:
            return {"error": "Words is required"}, 400

        words = request_json["words"]

        print("%s - Getting definitions for %s" % (language, words))

        try:
            with Pool(10) as pool:
                returned_definitions = {}
                for result in [
                    pool.apply_async(fetch_definitions, args=(language, word))
                    for word in words
                ]:
                    try:
                        definitions, word = result.get(5)
                        if definitions is not None:
                            print("Got definitions in %s for %s" % (language, word))
                            returned_definitions[word] = definitions
                        else:
                            print("No definition in %s found for %s" % (language, word))
                    except TimeoutError:
                        print("Definition lookup timed out")
                return returned_definitions, 200
        except Exception:
            print("Error getting definitions in %s for words: %s" % (language, words))
            stacktrace = traceback.format_exc()
            print(stacktrace)
            return {"error": "An error occurred"}, 500


class DocumentController(Resource):
    def post(self, language=None):
        if not is_authorized:
            return {"error": "No authorization provided"}, 401

        request_json = request.get_json()

        if language is None or language == "":
            return {"error": "Language is required"}, 400

        language = language.upper()
        if language not in SUPPORTED_LANGUAGES:
            return {"error": "Language %s is not supported" % language}, 400

        if request_json is None or "text" not in request_json:
            return {"error": "Text is required"}, 400

        text = request_json["text"]

        print("%s - Getting words for %s" % (language, text))

        try:
            return [word.to_json() for word in tag(language, text)], 200
        except Exception:
            print("Error getting words in %s for text: %s" % (language, text))
            stacktrace = traceback.format_exc()
            print(stacktrace)
            return {"error": "An error occurred"}, 500


class HealthController(Resource):
    def get(self):
        return {"message": "Language service is up"}, 200
