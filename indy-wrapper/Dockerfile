FROM maven:3.5-jdk-8
# Add pom first to cache deps
ADD pom.xml /
RUN mvn package

ADD . /

RUN mvn install

FROM ubuntu:16.04
# First install software-properties-common for add-apt-repository, and apt-transport-https to communicate.
RUN apt-get update \
    && apt-get install -y software-properties-common \
                openjdk-8-jre \
                apt-transport-https \
    && apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 \
    && add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial master"

ARG LIBINDY_VERSION

# Split off libindy command for fast builds on version bump
RUN apt-get update && apt-get install -y libindy=$LIBINDY_VERSION

COPY --from=0 /target/studybits-*-jar-with-dependencies.jar /studybits.jar

CMD java -cp /studybits.jar nl.quintor.studybits.Main