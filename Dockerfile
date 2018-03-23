FROM maven:3.5-jdk-8
ADD . /
RUN mvn package