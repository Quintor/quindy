#!/bin/bash
set -e
if [ ! -f $HOME/.m2/settings.xml ]; then
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"></settings>' > $HOME/.m2/settings.xml
fi

docker build -t studybits/base-image:local -f ci/base-image.dockerfile ci/
docker build -t studybits/indy-pool:local -f ci/indy-pool.dockerfile ci/

export TEST_POOL_IP=127.0.0.1
docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper
