from flask import request
from flask_restful import Resource

from language_service.controller.common import (
    check_authentication,
    check_language,
    get_required_field,
)
from language_service.dto.word import WordSchema
from language_service.service.tag import tag

word_schema = WordSchema(many=True)


class DocumentController(Resource):
    method_decorators = [check_authentication]

    def post(self, language=None):
        language, error = check_language(language)
        if error:
            return {"error": error}, 400

        text, error = get_required_field(request, "text")
        if error:
            return {"error": error}, 400

        print("Getting words in %s for %s" % (language, text))

        words = tag(language, text)
        return word_schema.dump(words), 200
