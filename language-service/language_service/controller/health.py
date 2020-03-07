from flask_restful import Resource


class HealthController(Resource):
    def get(self):
        return {"message": "Language service is up"}, 200
