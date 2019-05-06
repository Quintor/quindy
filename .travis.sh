#!/bin/bash
set -e
docker build -t studybits/base-image:local -f ci/base-image.dockerfile ci/
docker build -t studybits/indy-pool:local -f ci/indy-pool.dockerfile ci/
export TEST_POOL_IP=127.0.0.1
docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper
