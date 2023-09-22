#!/bin/bash

set -e
# set -x

docker-compose run --rm --workdir=/build appbuild
DOCKER_BUILDKIT=1 docker-compose build apprun --no-cache # for debugging build in compose V2: --progress=plain
docker-compose run --rm -e"KAIKO_API_KEY=${KAIKO_API_KEY}" apprun
