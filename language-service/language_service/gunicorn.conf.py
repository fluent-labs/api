# Mostly necessary to keep the health check from failing when a long running call happens
# If I revisit this then this stack overflow was pretty helpful:
# https://pythonspeed.com/articles/gunicorn-in-docker/
workers = 2
threads = 4
worker_class = "gevent"
worker_tmp_dir = "/dev/shm"  # nosec This is low risk
# This application is pretty well locked down in a container

bind = ":8000"

logconfig_dict = {
    "version": 1,
    "loggers": {
        "root": {"level": "INFO", "handlers": ["console"]},
        "gunicorn.error": {
            "level": "INFO",
            "handlers": ["console"],
            "propagate": 1,
            "qualname": "gunicorn.error",
        },
        "gunicorn.access": {
            "level": "INFO",
            "handlers": ["console"],
            "propagate": 0,
            "qualname": "gunicorn.access",
        },
    },
    "handlers": {"console": {"class": "logging.StreamHandler", "formatter": "json"}},
    "formatters": {
        "json": {
            "format": "%(message)%(levelname)%(name)%(asctime)%(pathname)",
            "class": "pythonjsonlogger.jsonlogger.JsonFormatter",
        }
    },
}
