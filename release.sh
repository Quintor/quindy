#!/usr/bin/env bash

read -p "Release version? (N/y) $1" -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    mvn versions:set -DnewVersion=$1

    docker build -t studybits/base-image -f ci/base-image.dockerfile ci/
    TEST_POOL_IP=127.0.0.1 DEPLOY=true docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper

    docker tag studybits/base-image "studybits/base-image:$1"
    docker tag studybits/indy-pool "studybits/indy-pool:$1"
    docker push "studybits/base-image:$1"
    docker push "studybits/indy-pool:$1"
fi
