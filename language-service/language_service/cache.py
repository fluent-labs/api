"""
A bit of an antipattern but this cache needs a module to hold it.
That way it can be imported into any file
https://stackoverflow.com/questions/48578906/flask-caching-multiple-files-in-project
"""
from flask_caching import Cache

# Here are some constants for how long to cache
HOUR = 3600
DAY = 86400
WEEK = 604800

cache = Cache()
