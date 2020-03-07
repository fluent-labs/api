from flask import request
from flask_restful import Resource

from language_service.controller.common import (
    check_authentication,
    check_language,
    get_required_field,
)
from language_service.service.definition import (
    get_definitions,
    get_definitions_for_group,
)
from language_service.dto.definition import DefinitionSchema


definition_schema = DefinitionSchema(many=True)


class DefinitionController(Resource):
    method_decorators = [check_authentication]

    def get(self, language=None, word=None):
        language, error = check_language(language)
        if error:
            return {"error": error}, 400

        if word is None or word == "":
            return {"error": "Word is required"}, 400

        print("Getting definition in %s for %s" % (language, word))

        definitions = get_definitions(language, word)
        return definition_schema.dump(definitions), 200

    def post(self, language=None):
        language, error = check_language(language)
        if error:
            return {"error": error}, 400

        words, error = get_required_field(request, "words")
        if error:
            return {"error": error}, 400

        print("Getting definitions in %s for %s" % (language, words))

        return (
            {
                word: definition_schema.dump(definition)
                for (word, definition) in get_definitions_for_group(language, words)
            },
            200,
        )
