#!/bin/bash
docker-compose up --build --force-recreate --exit-code-from tests
cd indy-wrapper
docker-compose up --build --force-recreate --exit-code-from wrapper
