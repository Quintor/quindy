FROM studybits:latest
FROM ubuntu:16.04
# First install software-properties-common for add-apt-repository, and apt-transport-https to communicate.
RUN apt-get update \
    && apt-get install -y software-properties-common \
                openjdk-8-jre \
                apt-transport-https \
    && apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 \
    && add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial stable"

# Set the default Libindy version. Will be overwritten when argument is supplied from console
ARG LIBINDY_VERSION=1.4.0

# Split off libindy command for fast builds on version bump
RUN apt-get update && apt-get install -y libindy=$LIBINDY_VERSION

COPY --from=0 /indy-wrapper/target/indy-wrapper-*-jar-with-dependencies.jar /indy-wrapper.jar

CMD java -jar /indy-wrapper.jar
