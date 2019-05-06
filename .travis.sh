#!/bin/bash
set -e
mkdir $HOME/.m2
export TEST_POOL_IP=127.0.0.1
docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper
