# Mostly necessary to keep the health check from failing when a long running call happens
# If I revisit this then this stack overflow was pretty helpful:
# https://pythonspeed.com/articles/gunicorn-in-docker/
workers = 2
threads = 4
worker_class = "gevent"
worker_tmp_dir = "/dev/shm"  # nosec This is low risk
# This application is pretty well locked down in a container

# We should filter the logs at the cluster level
loglevel = "debug"

bind = ":8000"
