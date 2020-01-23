#!/bin/sh
docker run --name foreign-language-reader -e MYSQL_ROOT_PASSWORD=my-secret-pw -p 3306:3306 -d mysql:5.7