#!/bin/sh

if $DEPLOY; then
    mvn clean package verify deploy;
else
    mvn verify;
fi;

if [ -n "$SONAR_CLOUD_TOKEN" ]; then
    if [ "$SONAR_BRANCH" = "master" ]; then
        mvn sonar:sonar \
        -Dsonar.projectKey=Quintor_quindy \
        -Dsonar.organization=quintor \
        -Dsonar.host.url=https://sonarcloud.io \
        -Dsonar.login=$SONAR_CLOUD_TOKEN
    else
        mvn sonar:sonar \
        -Dsonar.projectKey=Quintor_quindy \
        -Dsonar.organization=quintor \
        -Dsonar.host.url=https://sonarcloud.io \
        -Dsonar.login=$SONAR_CLOUD_TOKEN \
        -Dsonar.branch.name=$SONAR_BRANCH
    fi;
fi;