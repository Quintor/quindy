FROM ubuntu:16.04
# First install software-properties-common for add-apt-repository, and apt-transport-https to communicate.
RUN apt-get update \
    && apt-get install -y software-properties-common \
                apt-transport-https \
                maven \
    && apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 \
    && add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial stable" \
    && add-apt-repository ppa:openjdk-r/ppa \ 
    && apt-get update \
    && apt-get install -y openjdk-11-jdk
 
# Set the default Libindy version. Will be overwritten when argument is supplied from console
ARG LIBINDY_VERSION=1.9.0

# Split off libindy command for fast builds on version bump
RUN apt-get update && apt-get install -y libindy=$LIBINDY_VERSION
