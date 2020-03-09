# Mostly necessary to keep the health check from failing when a long running call happens
workers = 2
threads = 4
worker_class = "gevent"
worker_tmp_dir = "/dev/shm"
# If I revisit this then this stack overflow was pretty helpful:
# https://pythonspeed.com/articles/gunicorn-in-docker/

# We should filter the logs at the cluster level
loglevel = "debug"

bind = ":8000"
