#!/bin/bash
docker-compose up --build --force-recreate --exit-code-from tests pool backend-university backend-student tests
cd indy-wrapper
docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper
