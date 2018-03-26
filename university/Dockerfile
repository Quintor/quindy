FROM studybits:latest
FROM ubuntu:16.04
# First install software-properties-common for add-apt-repository, and apt-transport-https to communicate.
RUN apt-get update \
    && apt-get install -y software-properties-common \
                openjdk-8-jre \
                apt-transport-https \
    && apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 \
    && add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial master"

# Set the default Libindy version. Will be overwritten when argument is supplied from console
ARG LIBINDY_VERSION=1.3.1~406

# Split off libindy command for fast builds on version bump
RUN apt-get update && apt-get install -y libindy=$LIBINDY_VERSION

COPY --from=0 /university/target/university-*.jar /backend-university.jar

CMD java -jar /backend-university.jar