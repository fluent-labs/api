import os
from functools import wraps

from flask import request
from flask_restful import abort

AUTH_TOKEN = os.getenv("AUTH_TOKEN", "local")
SUPPORTED_LANGUAGES = ["CHINESE", "ENGLISH", "SPANISH"]


def check_authentication(method):
    """
    Makes sure the request contains the required authorization.
    Pretty much makes sure it was called internally or during a test
    """

    @wraps(method)
    def authentication_checker(*args, **kwargs):
        print("Something is happening before the function is called.")
        if (
            "Authorization" in request.headers
            and request.headers["Authorization"] == AUTH_TOKEN
        ):
            return method(*args, **kwargs)
        else:
            print("Aborting because request is not authorized")
            abort(401)

    return authentication_checker


def check_language(language):
    """
    Checks the language to see if it is valid.
    Returns either (language, None) on success or (None, error_message) on failure
    """
    if language is None or language == "":
        print("No language passed")
        return None, "Language is required"

    normalized_language = language.upper()

    if normalized_language not in SUPPORTED_LANGUAGES:
        print("Unsupported language %s requested" % language)
        return None, "Language %s is not supported" % language

    return normalized_language, None


def get_required_field(request, field_name):
    """
    Checks the request body for a required field
    Returns either (field, None) on success or (None, error_message) on failure
    """
    request_json = request.get_json()

    if request_json is None or field_name not in request_json:
        print("Required field %s was not included in request" % field_name)
        return None, "Field %s is required" % field_name

    return request_json[field_name], None
