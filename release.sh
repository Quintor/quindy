#!/usr/bin/env bash

read -p "Release version? $1" -n 1 -r
echo    # (optional) move to a new line
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    mvn versions:set -DnewVersion=$1

    docker build -t studybits/base-image:local -f ci/base-image.dockefile ci/
    docker build -t studybits/indy-pool:local -f ci/indy-pool.dockerfile ci/
    TEST_POOL_IP=127.0.0.1 DEPLOY=true docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper

    docker tag studybits/base-image:local "studybits/base-image:$1"
    docker tag studybits/indy-pool:local "studybits/base-image:$1"
    docker push "studybits/base-image:$1"
    docker push "studybits/base-image:$1"
fi
