#!/bin/bash
set -e 

docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper
